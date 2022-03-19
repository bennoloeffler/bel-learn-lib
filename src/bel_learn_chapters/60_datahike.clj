(ns bel-learn-chapters.60-datahike
  (:require [datahike.api :as d]
            [clojure.inspector :as insp]
            [taoensso.timbre :as log])
  (:import ;[javax.swing JFrame JLabel JButton]
           ;[java.awt.event WindowListener]
           (com.formdev.flatlaf FlatLightLaf FlatLaf FlatDarkLaf)))


(comment

  (log/merge-config! {:min-level :debug}) ; :level debug does nothing!

  ;; use the filesystem as storage medium
  (def cfg {:name "bels-db"
            :store {:backend :file :path "/tmp/example"}
            :schema-flexibility :read})
  (if-not (d/database-exists? cfg)
    (d/create-database cfg))
  ;; create a database at this place, per default configuration we enforce a strict
  ;; schema and keep all historical data


  (def conn (d/connect cfg))

  ;; the first transaction will be the schema we are using
  ;; you may also add this within database creation by adding :initial-tx
  ;; to the configuration
  (d/transact conn [{:db/ident :name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one}
                    {:db/ident :age
                     :db/valueType :db.type/long
                     :db/cardinality :db.cardinality/one}])

  ;; lets add some data and wait for the transaction
  (d/transact conn [{:name  "Alice", :age   20}
                    {:name  "Bob", :age   30}
                    {:name  "Charlie", :age   40}
                    {:age 15}])

  ;; search the data
  (d/q '[:find ?e ?n ?a
         :where
         [?e :name ?n]
         [?e :age ?a]]
     @conn)
  ;; => #{[3 "Alice" 20] [4 "Bob" 30] [5 "Charlie" 40]}

  (defn show [q-result]
    (FlatDarkLaf/install)
    (insp/inspect-table (vec q-result)))

  ;show the whole database
  (show (d/q '[:find ?e ?a ?v
               :where
               [?e ?a ?v]]
             @conn))


  ;; add new entity data using a hash map
  (d/transact conn {:tx-data [{:db/id 3 :age 26}]})

  ;; if you want to work with queries like in
  ;; https://grishaev.me/en/datomic-query/,
  ;; you may use a hashmap
  (d/q {:query '{:find [?e ?n ?a]
                 :where [[?e :name ?n]
                         [?e :age ?a]]}
        :args [@conn]})
  ;; => #{[5 "Charlie" 40] [4 "Bob" 30] [3 "Alice" 25]}

  ;; query the history of the data
  (d/q '[:find ?a
         :where
         [?e :name "Alice"]
         [?e :age ?a]]
      (d/history @conn))
  ;; => #{[20] [25]}

  ;; you might need to release the connection for specific stores like leveldb
  (d/release conn)

  ;; clean up the database if it is not need any more
  (d/delete-database cfg))
