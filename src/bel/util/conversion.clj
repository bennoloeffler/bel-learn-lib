(ns bel.util.conversion
  (:require [clojure.string :as str]
            [clojure.test :refer [is deftest]]
            [erdos.assert :as pa]
            [java-time :as jt]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; parsing string to data type
;;
;; d = german date, e.g. 13.2.2013
;; l = long, e.g. 123
;; n = number, e.g. 13.7
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn dt-str [local-data]
  ;(println local-data)
  (jt/format "d.M.yyyy hh:mm:ss" local-data))

(defn d-str [local-data]
  ;(println local-data)
  (jt/format "d.M.yyyy" local-data))


(defn d [string]
  (try
    (jt/local-date "d.M.yyyy" string)
    (catch Exception e
      (throw
        (Exception.
          (str "value \"" string "\" cannot be made"
               " a LocalDate, errMsg from parser=" (.getMessage e)))))))

(jt/format "d.M.yyyy" (d "21.3.2015"))

(comment                                                    ; some examples how to use d
  (pa/assert (= (jt/local-date 2015 12 22)
                (d "22.12.2015")))
  (pa/assert (= (jt/local-date 2015 1 2)
                (d "2.1.2015")))
  (d "22.1ERR2.2015")
  (d "1.1.2015")
  (d "11.11.2015"))


(defn l [string]
  (try
    (Long/parseLong string)
    (catch Exception e
      (throw (Exception.
               (str "value \"" string "\" cannot be made"
                    " a Long, errMsg from parser=" (.getMessage e)))))))

(comment
  (pa/assert (= 17 (l "17"))))


(defn n [string]
  (try
    (Double/parseDouble string)
    (catch Exception e
      (throw (Exception.
               (str "value \"" string "\" cannot be made"
                    " a Double, errMsg from parser=" (.getMessage e)))))))




(comment                                                    ; some examples how to use d
  (pa/assert (= 17.0 (n "17")))
  (pa/assert (!= 17.56 (n "17.5600000000000099")))
  (pa/assert (= 17.56 (n "17.5600000000000001")))
  (= 17.0 (n "x"))
  (pa/assert (!= 2 3)))


