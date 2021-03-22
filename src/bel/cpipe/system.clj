(ns bel.cpipe.system
  (:require [bel.cpipe.ui :as ui]
            [clojure.pprint :as pp]))

(defn system
  "returns a new instance of the whole system"
  []
  (println "CREATING the system...")
  {:frame "nix"})

(defn start
 "do all the side effects to start the system"
 [system]
 (println "STARTING the system")
 (-> (:frame system) println)
 system)

(defn stop
 "do all the side effects to stop the system"
 [system]
 (println "STOPPING the system")
 (-> (:frame system) println)
 system)

(defn -main [& args]
 (start (system)))

(comment
 (-main))