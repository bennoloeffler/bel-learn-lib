(ns bel.cpipe.model
  (:require [datahike.api :as d]
            [taoensso.timbre :as log]
            [java-time :as jt])
  (:gen-class))

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


(defn dev-cfg [] {:name "cpipe-db"
                  :store {:backend :file :path "/tmp/cpipe"}
                  :schema-flexibility :read})


(defn make-schema [conn]
  (d/transact conn (task-schema))
  (d/transact conn (project-schema))
  conn)




(defn init-db [cfg delete-db]
  (log/merge-config! {:min-level :info})
  (when (= delete-db :delete-db)
    (d/delete-database cfg))
  (when-not (d/database-exists? cfg)
    (d/create-database cfg))
  ; do schema in every case!
  (-> (d/connect cfg) make-schema))


(defn create-project [conn name delivery-date]
 (d/transact conn [{:project/name name :project/delivery-date delivery-date}]))


(defn add-task [conn p-name start end capa-need description]
 (d/transact conn [{:db/id -1 :task/start start :task/end end :task/capa-need capa-need :task/description description}
                   {:db/id [:project/name p-name] :project/tasks -1}]))


(comment
  (d/delete-database (dev-cfg))
  (let [conn (init-db (dev-cfg))]
    (d/transact conn [{:task/start (jt/local-date)
                       :task/end (jt/plus (jt/local-date) (jt/days 7))
                       :task/capa-need 4.4
                       :task/description "the task"}])))