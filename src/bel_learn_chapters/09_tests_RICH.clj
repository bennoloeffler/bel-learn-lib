(ns bel-learn-chapters.09-tests-RICH
  (:require [hyperfiddle.rcf :refer [tests]]
            [com.mjdowney.rich-comment-tests]))


; RICH Tests
; https://github.com/hyperfiddle/rcf

; run with lein
; https://bytemeta.vip/repo/hyperfiddle/rcf/issues/53


;(hyperfiddle.rcf/enable!)
(hyperfiddle.rcf/enable! false)


(defn square
  "this is the test subject"
  [x] (* x x))

(tests
  (hyperfiddle.rcf/enable!)
  (square 6) := 36
  (tests
    81 := (square 9)
    82 :<> (square 9)))

(tests
   true := (number? 4))

;; may use matcho patterns: https://github.com/HealthSamurai/matcho
^:rct/test
(comment
  (+ 1 1) ;=> 2
  (range 3) ;=> (0 1 2)
  ; matcho pattern with =>>
  (+ 2 3)  ;=>> int?
  (+ 2 3)  ;=>> string?
  nil)

(comment
  (com.mjdowney.rich-comment-tests/run-ns-tests! *ns*))


