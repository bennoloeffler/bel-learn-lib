(ns bel-learn-chapters.61-datahike-model-query
  (:require [datahike.api :as d]
            [clojure.inspector :as insp])
  (:import
    ;[javax.swing JFrame JLabel JButton]
    ;[java.awt.event WindowListener]
    (java.util Date)
    (com.formdev.flatlaf FlatLightLaf FlatLaf FlatDarkLaf)))


; https://cljdoc.org/d/io.replikativ/datahike/0.3.3/doc/readme
; https://docs.datomic.com/on-prem/overview/introduction.html
; https://github.com/kristianmandrup/datascript-tutorial/blob/master/SUMMARY.md
; http://www.learndatalogtoday.org/

(def cfg {:name               "bels-db"
          :store              {:backend :file :path "/tmp/example"}
          :schema-flexibility :read})

(defn create-connect-db []
  (if-not (d/database-exists? cfg)
    (d/create-database cfg))
  (def conn (d/connect cfg)))


(defn delete-db []
  ;; you might need to release the connection for specific stores like leveldb
  ;(d/release conn)
  ;; clean up the database if it is not need any more
  (d/delete-database cfg))


(defn delete-recreate-db []
  (delete-db)
  (create-connect-db))

(defn create-schema []

  ; person (name, sur-name, born, email, uses-cars, weight)
  ; car (manufacturer, model, built, license-plate)

  (d/transact conn [{:db/ident       :person/name
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident       :person/sur-name
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident       :person/born
                     :db/valueType   :db.type/long
                     :db/cardinality :db.cardinality/one}

                    {:db/ident       :person/email
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/unique      :db.unique/value}

                    {:db/ident       :person/uses-cars
                     :db/valueType   :db.type/ref
                     :db/cardinality :db.cardinality/many}

                    {:db/ident       :person/weight
                     :db/valueType   :db.type/double
                     :db/cardinality :db.cardinality/one}


                    {:db/ident       :car/manufacturer
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident       :car/model
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident       :car/built
                     :db/valueType   :db.type/long
                     :db/cardinality :db.cardinality/one}

                    {:db/ident       :car/license-plate
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/unique      :db.unique/value}]))

(defn create-sample []
  (d/transact conn [{:car/manufacturer  "BMW",
                     :car/model         "325",
                     :car/built         2001
                     :car/license-plate "S-GH-5443"}

                    {:car/manufacturer  "BMW",
                     :car/model         "530d",
                     :car/built         2019
                     :car/license-plate "H-VS-898"}

                    {:db/id             -1,
                     :car/manufacturer  "Subaru",
                     :car/model         "Justy",
                     :car/built         1991,
                     :car/license-plate "TBB-SK-123"}

                    {:person/name      "Alice",
                     :person/sur-name  "Cooper",
                     :person/born      1947
                     :person/email     "alice.cooper@animal.com"
                     :person/uses-cars [{:db/id [:car/license-plate "S-GH-5443"]}]}

                    {:person/name      "John",
                     :person/sur-name  "Lennon",
                     :person/born      1941
                     :person/email     "jo.le@heaven.de"
                     :person/uses-cars [{:db/id [:car/license-plate "H-VS-898"]}
                                        {:db/id [:car/license-plate "S-GH-5443"]}
                                        -1]}

                    {:person/name      "Angela",
                     :person/sur-name  "Merkel",
                     :person/born      1960
                     :person/email     "angela.merkel@leader.de"
                     :person/uses-cars -1}]))



(comment
  (delete-recreate-db)
  (create-schema)
  (create-sample)

  ;; search the cars
  (d/q '[:find ?e ?ma ?mo ?p ?b
         :where
         [?e :car/manufacturer ?ma]
         [?e :car/model ?mo]
         [?e :car/built ?b]
         [?e :car/license-plate ?p]]
       @conn)

  ;; search all persons with their cars
  (d/q '[:find ?e ?n ?sn ?model
         :where
         [?e :person/name ?n]
         [?e :person/sur-name ?sn]
         [?e :person/uses-cars ?c]
         [?c :car/model ?model]]
       @conn)

  ;; search persons that use a specific car
  (d/q '[:find ?n ?sn
         :in $ ?license-plate
         :where
         [?e :person/name ?n]
         [?e :person/sur-name ?sn]
         [?e :person/uses-cars ?c]
         [?c :car/license-plate ?license-plate]]
       @conn "S-GH-5443")

  ; get entity-ID (:db/id) of car by license-plate (:db/unique :db.unique/value)
  (d/q '[:find ?e
         :in $ ?license-plate
         :where [?e :car/license-plate ?license-plate]]
       @conn
       "S-GH-5443")

  ;; search persons that use a specific car (backwards = _)
  (:person/_uses-cars (d/entity @conn [:car/license-plate "S-GH-5443"]))

  ; add the current weight
  (d/transact conn [{:db/id         [:person/email "angela.merkel@leader.de"]
                     :person/weight 62}])

  (defn transact-person-weight [email weight]
    (d/transact conn [{:db/id         [:person/email email]
                       :person/weight weight}]))

  (transact-person-weight "angela.merkel@leader.de" 61)
  (transact-person-weight "angela.merkel@leader.de" 83)

  ; show all persons with a weight connected
  (d/q '[:find ?n ?sn ?w
         :where
         [?e :person/name ?n]
         [?e :person/sur-name ?sn]
         [?e :person/weight ?w]]
       @conn)

  ; remove a car from a person as user
  (d/transact conn [[:db/retract [:person/email "angela.merkel@leader.de"] :person/uses-cars [:car/license-plate "TBB-SK-123"]]])

  ; change the unique id
  (d/transact conn [{:db/id        [:person/email "angela.merkel@leader.de"]
                     :person/email "angela.merkel@firstlady.de"}])

  ; pull all attributes from entity
  (d/pull @conn ["*"] [:person/email "angela.merkel@firstlady.de"])

  ; visual table view for query results
  (defn show [q-result]
    (FlatDarkLaf/install)
    (insp/inspect-table (vec q-result)))

  ;show the whole database: all datoms (without tx and #inst)
  (show (d/q '[:find ?e ?a ?v
               :where
               [?e ?a ?v]]
             @conn))

  ;get all ids of people with email
  (def ids (d/q '[:find ?e
                  :where
                  [?e :person/email]]
                @conn))
  ;make them a vector
  (def ids (vec (flatten (vec ids))))

  ; persons are different... Angela has weight... others cars
  (show (d/pull-many @conn ["*"] ids))

  ; query the history of the data of Angelas weight, and sort
  (->> (d/q '[:find ?w ?timestamp
              :where
              [?e :person/name "Angela"]
              [?e :person/weight ?w ?tx true]               ; otherwise, true and false...
              [?tx :db/txInstant ?timestamp]]
            (d/history @conn))
       (sort #(- (.getTime (get %1 1))
                 (.getTime (get %2 1)))))

  ; query the history of the data - including add (true) and distract (false) markers
  (->> (d/q '[:find ?w ?timestamp ?tf
              :where
              [?e :person/name "Angela"]
              [?e :person/weight ?w ?tx ?tf]
              [?tx :db/txInstant ?timestamp]]
            (d/history @conn))
       (sort #(- (.getTime (get %1 1))
                 (.getTime (get %2 1))))))
