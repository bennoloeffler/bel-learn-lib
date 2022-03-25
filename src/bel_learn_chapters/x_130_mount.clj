(ns bel-learn-chapters.x-130-mount
  [:import [java.util Date]]
  [:require [mount.core :as m :refer [defstate]]])

; https://github.com/tolitius/mount#mount-up

; https://github.com/stuartsierra/component.repl
; https://lambdaisland.com/blog/2018-02-09-reloading-woes
; https://www.cbui.dev/a-tutorial-to-stuart-sierras-component/
; https://medium.com/@maciekszajna/reloaded-workflow-out-of-the-box-be6b5f38ea98



(defn create-bel-component []
  ;(println "starting bel-component")
  (atom {:model   {}
         :creation-date (Date.)}))

(defn release-bel-component [comp]
  ;(println "stopping bel-component")
  (assoc @comp :model nil))

(defstate bel-component
          :start (create-bel-component)
          :stop (release-bel-component bel-component))

(Thread/sleep 200)
(def glob bel-component)

(defn start []
  (m/start #'bel-learn-chapters.x-130-mount/bel-component))

(defn stop []
  (m/stop #'bel-learn-chapters.x-130-mount/bel-component))

