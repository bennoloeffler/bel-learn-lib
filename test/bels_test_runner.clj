(ns bels-test-runner
  (:require [clojure.test :refer :all]))

;; ALT -> WIN then T
(defn call-current-tests []
 (println "(ALT -> WIN then T)")
 (run-all-tests #"bel.*|")) ;#"bel.*")) ; bel.*test|clj-app.*test.*;

(comment
  (call-current-tests))