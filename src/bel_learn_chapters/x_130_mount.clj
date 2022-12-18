(ns bel-learn-chapters.x-130-mount
  [:import [java.util Date]]
  [:require [mount.core :as m :refer [defstate]]])

; https://github.com/tolitius/mount#mount-up

; https://github.com/stuartsierra/component.repl
; https://lambdaisland.com/blog/2018-02-09-reloading-woes
; https://www.cbui.dev/a-tutorial-to-stuart-sierras-component/
; https://medium.com/@maciekszajna/reloaded-workflow-out-of-the-box-be6b5f38ea98

;; https://mccue.dev/pages/12-7-22-clojure-web-primer
;; https://www.emcken.dk/programming/2019/02/06/clojure-testing-recent-findings/
;; https://grishaev.me/en/clj-book-systems/


(defn create-bel-component []
  (println "starting bel-component")
  (atom {:model   {}
         :creation-date (Date.)}))


(defn release-bel-component [comp]
  (println "stopping bel-component")
  (assoc @comp :model nil))

(defstate bel-component
          :start (create-bel-component)
          :stop (release-bel-component bel-component))

;(Thread/sleep 200)
(def glob bel-component)

(defn start []
  (m/start #'bel-learn-chapters.x-130-mount/bel-component))

(defn stop []
  (m/stop #'bel-learn-chapters.x-130-mount/bel-component))
