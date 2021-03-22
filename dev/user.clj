(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [puget.printer :refer (cprint)]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [debux.core :refer :all]
            [hashp.core :refer :all]
            [bel.cpipe.system :as system]
            [bels-test-runner]))


(def system nil)

(defn init
  "Constructs the current development system."
  []
  (alter-var-root #'system
    (constantly (system/system))))

(defn start
  "Starts the current development system."
  []
  (alter-var-root #'system system/start))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (alter-var-root #'system
    (fn [s] (when s (system/stop s)))))

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn run-bels-tests []
   (bels-test-runner/call-current-tests))


(comment
  (go)
  (stop)
  (start)
  (init)
  (reset)
  (pprint system))

; RUNNING TESTs per WATCHER all the time
; lein bat-test auto

(comment
  (set! *print-length* 100)
  (cprint {:key1 'abc :key2 "qwertz" :key3 1.34})
  (pprint {:key1 'abc :key2 "qwertz" :key3 1.34})
  (/ 1 0)
  (pst)
  (bels-test-runner/call-current-tests)
  (run-bels-tests)
  (->> (all-ns) (filter #(re-find (re-pattern "datomic") (str %))))
  (map #(ns-name %) (all-ns))
  (dbg (->> (all-ns) (shuffle) (take 20) (map ns-name) sort (partition 4)))
  (/ 10 #p (/ (- 12 10) (+ 10 1))))

