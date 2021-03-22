(ns bels-test-runner
  (:require [clojure.test :refer :all])
  (:gen-class))

;; CTRL-WIN-ALT T
(defn call-current-tests []
 (println "namespace bels-test-runner --> call-current-tests (STRG-WIN-ALT T)")
 (run-all-tests #"bel.cpipe.*-test")) ; bel.*test|clj-app.*test.*;

(comment
  (call-current-tests))