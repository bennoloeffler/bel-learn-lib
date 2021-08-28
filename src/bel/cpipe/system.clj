(ns bel.cpipe.system
  (:require [bel.cpipe.model :as model]
            ;[clojure.pprint :as pp]
            [java-time :as jt]
            [puget.printer :as pp]))

(defn system
  "returns a new instance of the whole system"
  []
  (println "CREATING the system...")
  {:created (jt/local-date-time)
   :del-db :delete-db})
   ;:cfg (model/dev-cfg)})

(defn start
 "do all the side effects to start the system"
 [system]
 (println "STARTING the system...")
 (system))
 ;(assoc system :conn (model/init-db (:cfg system) (:del-db system))))

(defn stop
 "do all the side effects to stop the system"
 [system]
 (print "STOPPING the system: ")
 ;(pp/cprint system)
 system)

(defn -main [& args]
 (start (system)))

(comment
 (-main))