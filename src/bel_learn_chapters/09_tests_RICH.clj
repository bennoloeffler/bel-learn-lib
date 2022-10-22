(ns bel-learn-chapters.09-tests-RICH
  (:require [hyperfiddle.rcf :refer [tests]]))


; RICH Tests
; https://github.com/hyperfiddle/rcf

; run with lein
; https://bytemeta.vip/repo/hyperfiddle/rcf/issues/53


(hyperfiddle.rcf/enable!)


(defn square [x] (* x x))

(tests
  (square 6) := 36)