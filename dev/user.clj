(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :as pp :refer (pprint)]
            [puget.printer :refer (cprint)]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.reflect :as reflect]
            [clojure.inspector :as insp]
            [clojure.java.javadoc :as jdoc]
            [clojure.tools.namespace.repl :refer (refresh refresh-all clear)]
            [debux.core :refer :all]
            [hashp.core :refer :all]
            [bel.cpipe.system :as system]
            [bels-test-runner :refer [call-current-tests]])
  (:use tupelo.core))


; intellij-short-cuts

; mac:   win:
; shft ⇧  =  shft
; ctrl ⌃  =  ctrl
; cmnd ⌘  =  alt
; all: s c a = shift ctrl alt = ⇧ ⌃ ⌘

;   c    D Repl Debug
;   c a  U Update Project

; s c    L Load File to Repl
; s c    R switch Repl to namespace

; s   a  G go system (in user)
; s   a  R Reset system (in user)
; s   a  S Sync all files

; s c a  T Testing actions
; s c a  S Structural editing action
; s c a  R Repl actions
; s c a  M Move actions




; https://github.com/stuartsierra/component.repl
; https://lambdaisland.com/blog/2018-02-09-reloading-woes
; https://www.cbui.dev/a-tutorial-to-stuart-sierras-component/
; https://medium.com/@maciekszajna/reloaded-workflow-out-of-the-box-be6b5f38ea98

(def last-system nil)
(def system nil)

#_(def emtpy-system [:system "no system! see user/init"])


(defn init
  "Constructs the current development system."
  []
  #_emtpy-system
  (alter-var-root #'system
                  (constantly (system/init))))

(defn start
  "Starts the current development system."
  []
  #_emtpy-system
  (if system
    (alter-var-root #'system system/start)
    (println "no system - nothing to start")))

(defn stop
  "Shuts down and destroys the current development system."
  []
  #_emtpy-system
  (if system
    (alter-var-root #'system
                    (fn [s]
                      (when s (system/stop s))
                      (def last-system s)
                      nil))
    (println "no system - nothing to stop")))

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start)
  (println "initialized and started!"))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn tests []
  (println "__________________________________________________________________")
  (println "")
  (println "REFRESH and RUN ALL TESTS with bels-test-runner/call-current-tests")
  (println "__________________________________________________________________")
  (refresh :after 'bels-test-runner/call-current-tests))

(defn overview []
  (bel-learn-lib.package-viewer/-main))

(comment
  (go)
  (stop)
  (start)
  (tests)
  (init)
  (reset)
  (refresh)
  (refresh-all)
  (overview)
  (pprint system))

; RUNNING TESTs per WATCHER all the time
; use profile in order to switch on humane-test-output etc in terminal and keep
; it off in intellij
; lein with-profile bel-test  bat-test auto

(comment
  (require '[clojure.string :as str :refer [upper-case]])
  (doc nil?)
  (source some?)
  (dir str)
  (dir clojure.repl)
  (apropos "time")
  (find-doc "time")
  (set! *print-length* nil)
  (set! *print-level* nil)
  (cprint {:key1 'abc :key2 "qwertz" :key3 1.34})
  (pprint {:key1 'abc :key2 "qwertz" :key3 1.34})
  (/ 1 0)
  (pst)
  *e
  (- 4 5 (+ 1 24))
  (println *1 *2)
  (def v (io/input-stream "https://www.clojure.org"))
  (ancestors (type v))
  (jdoc/javadoc java.io.InputStream)
  (dir reflect)
  (reflect/reflect java.io.InputStream)
  (->> *1 :members (sort-by :name) (pp/print-table [:name :flags :parameter-types :return-type]))
  (insp/inspect-table (repeat 5 {:key1 'abc :key2 "qwertz" :key3 1.34}))
  (bels-test-runner/call-current-tests)
  (->> (all-ns) (filter #(re-find (re-pattern "bel") (str %))))
  (map #(ns-name %) (all-ns))
  (dbg (->> (all-ns) (shuffle) (take 3) (map ns-name) sort (partition 4)))
  (/ 10 #p (/ (- 12 10) (+ 10 1)))

  (defn s []
    (constantly (do (println "again...") (+ 20 100))))
  (dbg 3)
  ((s) 1 2 3))
