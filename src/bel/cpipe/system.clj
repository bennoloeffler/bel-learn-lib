(ns bel.cpipe.system
  (:require [bel-learn-lib.package-viewer :as pv]
    ;[clojure.pprint :as pp]
            [java-time :as jt]
            [puget.printer :as pp]
            [clojure.java.io :as jio]))

(def state (atom {}))

(defn system
  "returns a new instance of the whole system"
  []
  (println "CREATING the system...")
  {:system-created (jt/local-date-time)
   :frame nil})

(defn start
  "do all the side effects to start the system"
  [system]
  (let [s (assoc system :frame (pv/create-frame))]
    (println "STARTING the system...")
    ;(pp/cprint s)
    (pv/show-frame (s :frame))
    s))


(defn stop
  "do all the side effects to stop the system"
  [system]
  (print "STOPPING the system: ")
  (if-let [s (:frame system)]
    (pv/dispose-frame s)
    (println "...there is nothing to stop"))
  (assoc system :frame nil))



(defn -main [& args]
  (start (system)))

(comment
  (-main))