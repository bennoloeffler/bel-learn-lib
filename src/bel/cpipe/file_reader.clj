(ns bel.cpipe.file-reader
  (:require [clojure.string :as str]
            [clojure.test :refer [is]]
            [erdos.assert :as pa]
            [java-time :as jt])
            ;[puget.printer :refer (cprint)])

  (:import (java.time LocalDate)))

;(use 'hashp.core)
;(use 'debux.core)

#_(defmacro dbg-bel [body]
    `(let [body# ~body]
       (println "-------- dbg-bel --------------")
       (cprint ['~body :--> body#])
       (println)
       body#))


(defn ct
  "create task with unnamed args"
  [project start end resource capacity comment]
  {:project  project :start start :end end
   :resource resource :capacity capacity :comment comment})


(defn d [string]
  (jt/local-date "d.M.yyyy" string))
(comment
  (pa/assert (= (jt/local-date 2015 12 22)
                (d "22.12.2015")))
  (pa/assert (= (jt/local-date 2015 1 2)
                (d "2.1.2015"))))

(defn l [string]
  (Long/parseLong string))

(defn n [string]
  (Double/parseDouble string))

(comment
  (pa/assert (= 17 (n"17"))))

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
  (let [result (condp = type ; case type did not work?
                 LocalDate (d data)
                 Double (n data)
                 Long (l data)
                 String data)]
    ; TODO: throw exception if wrong type
    (or result (throw (Exception. "type not supported: " type)))))
(comment
  (type (parse-type "17" Long))
  (parse-type "17.5.2022" LocalDate))


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




(defn parse-text-tasks
  "make one string line a task"
  [text]
  (let [result (parse-text
                 text
                 [String LocalDate LocalDate String Double String]
                 [:project :start :end :resource :capacity :comment])]
    result))
(comment
  (pa/assert (=
               (parse-text-tasks
                  "proj1  22.3.2016 24.6.2017 res1   22.0      comment-it-1\n\n
                  proj2  22.3.2017 24.6.2018 res1   23      comment-it-2\n
                  ")
               [(ct "proj1" (d "22.3.2016") (d "24.6.2017") "res1" (i "22") "comment-it-1")
                (ct "proj2" (d "22.3.2017") (d "24.6.2018") "res1" (i "23") "comment-it-2")])))


(defn parse-file-tasks [file-str]
  (-> file-str
      slurp
      parse-text-tasks))
(comment
  (parse-file-tasks "cpipe-test-files/Projekt-Start-Ende-Abt-Kapa.txt")
  (parse-file-tasks "cpipe-test-files/bsp-01-nur-tasks/Projekt-Start-End-Abt-Kapa.txt"))