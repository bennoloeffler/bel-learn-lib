(ns bel-learn-chapters.09-tests-RICH
  (:require [hyperfiddle.rcf :refer [tests]]))


; RICH Tests
; https://github.com/hyperfiddle/rcf

; run with lein
; https://bytemeta.vip/repo/hyperfiddle/rcf/issues/53


(hyperfiddle.rcf/enable!)


(defn square
  "this is the test subject"
  [x] (* x x))

(tests
  (square 6) := 36
  (tests
    81 := (square 9)
    82) :<> (square 9))

(tests
   true := (number? 4))
