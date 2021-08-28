(ns bel-learn-lib.core
  (:gen-class))


; HOW to use the lib
; ;;;;;;;;;;;;;;;;;;
; Make a dir in the other project
; c:\projects\bel-use-learn-lib\src\bel-learn-lib
; BASICALLY: bel-learn-lib in src
; Put a symbolic link there: 
; mklink ..\..\..\..\bel-learn-lib\src\ (AS ADMIN)
; THATS IT

(defn test-the-lib
  "test function to be called"
  []
  (println "clj-learn-lib.core/test-the-lib seems to work! Hello, World!")
  "result is comming back... lib works!")

(defn partition-by-nums [nums coll]
  (loop [coll coll nums nums rslt []]
    (if (or (empty? coll) (empty? nums))
      rslt
      (recur
        (drop (first nums) coll)
        (rest nums)
        (conj rslt (take (first nums) coll))))))

(defn get-digits [num]
  (map #(Character/digit % 10) (seq (str num))))

(comment
  (+ 3 4)
  (bel-learn-lib.core/test-the-lib)
  (type (first (get-digits 1235689000999999999999999999999999))))

