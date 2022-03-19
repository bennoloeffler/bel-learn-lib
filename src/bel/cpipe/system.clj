(ns bel.cpipe.system
  (:require [bel-learn-lib.package-viewer :as pv]
    ;[clojure.pprint :as pp]
            [java-time :as jt]
            [puget.printer :as pp]
            [clojure.java.io :as jio]
            [bel.cpipe.model :as model :refer :all]
            [bel.util.conversion :refer :all]
            [bel.cpipe.ui :as ui :refer :all]))


(defn init
  "returns a new instance of the whole system"
  []
  (println "INIT the system...")
  (let [data (model/read-test-model)]
    {:created (dt-str (jt/local-date-time))
     :data    data
     :frame   (ui/create-frame)}))

(defn start
  "do all the side effects to start the system"
  [system]
  (println "STARTING the system...")
  ;(pp/cprint system)
  (-> ( :frame system) ui/show-frame)
  system)


(defn stop
  "do all the side effects to stop the system"
  [system]
  (print "STOPPING the system: ")
  (doto (:frame system) (.setVisible false) .dispose))
  ;(if-let [s (:frame system)]
    ;(pv/dispose-frame s)
  ;(println "...there is nothing to stop")
  ;(assoc system :frame nil))




(defn -main [& args]
  (start (init)))

(comment
  (-main))