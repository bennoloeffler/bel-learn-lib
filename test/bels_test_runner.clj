(ns bels-test-runner
  (:require [clojure.test :refer :all])
  (:gen-class))




;; ALT-WIN Pause T
(defn call-current-tests []
 (println "namespace bels-test-runner --> call-current-tests (ALT-WIN -let-go- T)")
 (run-all-tests #"bel.*")) ; bel.*test|clj-app.*test.*;

(comment
  (call-current-tests))