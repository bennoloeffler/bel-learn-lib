(ns bel.cpipe.model
  (:require [datahike.api :as d]
            [taoensso.timbre :as log]))

(defn task-schema [] [{:db/ident :task/start
                       :db/valueType :db.type/instant
                       :db/cardinality :db.cardinality/one}

                      {:db/ident :task/end
                       :db/valueType :db.type/instant
                       :db/cardinality :db.cardinality/one}

                      {:db/ident :task/capa-need
                       :db/valueType :db.type/float
                       :db/cardinality :db.cardinality/one}

                      {:db/ident :task/description
                       :db/valueType :db.type/string
                       :db/cardinality :db.cardinality/one}])

(defn project-schema [] [{:db/ident :project/name
                          :db/valueType :db.type/string
                          :db/cardinality :db.cardinality/one
                          :db/unique :db.unique/value}

                         {:db/ident :project/delivery-date
                          :db/valueType :db.type/instant
                          :db/cardinality :db.cardinality/one}

                         {:db/ident :project/tasks
                          :db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/many}])


(log/merge-config! {:min-level :info})

(defn create-connect-db [cfg]
  (if-not (d/database-exists? cfg)
    (d/create-database cfg))
  (d/connect cfg))

(defn dev-cfg [] {:name "cpipe-db"
                  :store {:backend :file :path "/tmp/cpipe"}
                  :schema-flexibility :read})

(def conn {})

(defn init-db [cfg]
  (d/delete-database cfg)
  (def conn (create-connect-db cfg))
  ;(println conn)
  (d/transact conn (task-schema))
  (d/transact conn (project-schema)))

(comment
  (init-db (dev-cfg))
  (d/transact conn [{:task/start (java.util.Date.)
                     :task/end (java.util.Date.)
                     :task/capa-need 4.4
                     :task/description "the task"}]))