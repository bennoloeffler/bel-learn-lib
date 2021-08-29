(ns bel.cpipe.system
  (:require [bel.cpipe.model :as model]
            ;[clojure.pprint :as pp]
            [java-time :as jt]
            [puget.printer :as pp]))

(def state (atom {}))

(defn system
  "returns a new instance of the whole system"
  []
  (println "CREATING the system...")
  {:system-created (jt/local-date-time)})
   ;:model (model/new-empty-model)
   ;:ui (ui/create-and-glue-ui ())})

(defn start
 "do all the side effects to start the system"
 [system]
 (println "STARTING the system cpipe...")
 (system))
 ;(assoc system :conn (model/init-db (:cfg system) (:del-db system))))

(defn stop
 "do all the side effects to stop the system"
 [system]
 (print "STOPPING the system: ")
 (pp/cprint system)
 system)

(defn -main [& args]
 (start (system)))

(comment
 (-main))