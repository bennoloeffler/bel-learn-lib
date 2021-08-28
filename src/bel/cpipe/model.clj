(ns bel.cpipe.model
  (:require [clojure.core.specs.alpha :as s]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [java-time :as t])
  (:gen-class))

(defn str-is-date?
  "check if Date of
   format: 3.12.2021"
  [str]
  (re-matches #"\d{1,2}.\d{1,2}.\d{4}" str))

(defn str-to-date
  "parse Date (from String)
   format: 3.12.2021"
  [str]
  (t/local-date "d.M.yyyy" str))

(defn str-is-long?
  "check, if string is long number"
  [st]
  (let [digits (set (map str (range 10)))]
    (->> (map #(contains? digits %) (map str (seq st)))
         (every? true?))))

(defn str-to-long
  "parse str to long"
  [str]
  (Long/valueOf str))

(str-num? "12")


(defn slurp-lines
  "Reads all data lines in file, example:
     data1  data2 data3
     data4  5  6 7 8
   and returns them as
     [ [data1 data2 data3]
       [data4 5 6 7 8] ]
   Removes blank lines"
  [file-name]
  (as-> (slurp file-name) $
        (str/split $ #"\n")
        (map (fn [s] (filter not-empty (str/split s #"\s"))) $)
        (filter not-empty $)))


(def project-start-end-abt-kapa [{:validator nil :converter nil :err-msg ""}
                                 {:validator str-is-date? :converter str-to-date :err-msg "is not a valid date"}
                                 {:validator str-is-date? :converter str-to-date :err-msg "is not a valid date"}
                                 {:validator nil :converter nil :err-msg ""}
                                 {:validator str-is-long? :converter str-to-long :err-msg "is not a long"}
                                 {:validator nil :converter nil :err-msg "" :optional true}])

(as-> "C:\\projects\\v-pipe\\bsp-daten\\bsp-01-nur-tasks\\Projekt-Start-End-Abt-Kapa.txt" $
      (slurp-lines $)
      (map (fn [line] [(nth line 0)
                       (pd (nth line 1))
                       (pd (nth line 2))
                       (nth line 3)
                       (Long/valueOf (nth line 4))
                       (if (= 6 (count line)) (nth line 5) nil)]) $))