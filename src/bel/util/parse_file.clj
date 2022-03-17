(ns bel.util.parse-file
  (:require [clojure.string :as str]
            [clojure.test :refer [is deftest]]
            [erdos.assert :as pa]
            [bel.util.conversion :refer :all])
  (:import (java.time LocalDate LocalTime)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; parsing a file to data types
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn split-line [string]
  (as-> string it
        (str/split it #"\s")
        (remove #(= % "") it)))
(comment
  (pa/assert (= ["sf" "lsdf" "sfdgsd"] (split-line " sdf lsdf    sfdgsd   "))))


(defn map-line
  "if there is more data than keys: ignore.
   if there is less: just dont fill keys"
  [col keys]
  (let [result (apply hash-map (interleave keys col))]
    result))
(comment
  (pa/assert (= (map-line ["axx" "bx" "cxxxx-xx"] [:aa :bb :cc])
                {:aa "axx" :bb "bx" :cc "cxxxx-xx"})))


(defn separate-lines
  "separate lines with whitespace and many \n to
   collection of trimmed lines without the empty ones"
  [string]
  (as-> string it
        (str/trim it)
        (str/split it #"\n")
        (map str/trim it)
        (remove #(= % "") it)))
(comment
  (pa/assert (= (separate-lines "\n1a b\n \n  2cde fg\n \n ")
                ["1a b" "2cde fg"])))


(defn parse-type
  "parse LocalDate, Double or String - according to type"
  [data type]
  ;(println "d: " data "  t: " type)
  (let [result (condp = type                                ; case type did not work?
                 LocalDate (d data)
                 Double (n data)
                 Long (l data)
                 String data
                 nil)]
    (or result (throw (Exception. (str "type not supported: " type))))))
(comment
  (type (parse-type "17" Long))
  (parse-type "17.5.2022" LocalDate)
  (parse-type "17.5.2022" LocalTime))


(defn parse-typed-line [str-col types]
  (let [parsed (map parse-type str-col types)]
    parsed))
(comment
  (pa/assert (= ["str", 17] (parse-typed-line ["str" "17"] [String Double]))))


(defn parse-text
  "A text (e.g. from a file) consisting of lines separated by \n.
   Lines may contain whitespace and empty lines.
   Types to parse are line-types, e.g.: [LocalDate, Long, String]
   Keys to give are line-keys, e.g.: [:when :how-much :description]
   Given the text
   \"3.5.2012   42 short\n \n
   1.5.2012 23 no\"
   the result is something like:
   ({:when LocalDate(3.5.2012) :how-much 42 :description \"short\" }
    {...})
   "
  [text line-types line-keys]
  (let [lines       (separate-lines text)
        words       (map split-line lines)
        typed-lines (map #(parse-typed-line % line-types) words)
        keyed       (map #(map-line % line-keys) typed-lines)]
    keyed))
(comment
  (pa/assert (= [{:one "ab", :two 17} {:one "cd", :two 18}]
                (parse-text "ab 17\n cd  18" [String Long] [:one :two])))
  (parse-text "3.5.2012   42 short\n
               \n1.5.2012 23 no"
              [LocalDate, Long, String]
              [:when :how-much :description]))

