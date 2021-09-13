(ns bel-learn-chapters.x-130-mount
  [:import [java.util Date]]
  [:require [mount.core :as m :refer [defstate]]])

; https://github.com/tolitius/mount#mount-up
; 

(def bel-state (atom {}))

(defn create-bel-component []
  (println "starting bel-component")
  {:model   {}
   :creation-date (Date.)})

(defn release-bel-component [comp]
  (println "stopping bel-component")
  (assoc comp :model nil))

(defstate bel-component
          :start (create-bel-component)
          :stop (release-bel-component bel-component))

(m/start #'bel-learn-chapters.x-130-mount/bel-component)
