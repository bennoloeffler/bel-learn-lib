(ns bel.cpipe.week-cal
  (:require [java-time :as jt])
  (:import [java.time.temporal ChronoField WeekFields]
           [java.time LocalDate]
           [java.util Locale]))

;; a linear week model since 2010-01-04 until 2039-12-31 (including).

;; the right temporal field to get linear calender weeks
(def temporal-field-calender-week
  (.weekOfWeekBasedYear (WeekFields/of (Locale/getDefault))))

(defn calender-week
  "Calender week of the local date."
  [d]
  (.get d temporal-field-calender-week))

(defn- date-infos-as-vec [d]
  (let [y (.get d ChronoField/YEAR)
        w (calender-week d)]
    [y w]))

(defn- calc-abs-weeks
  "Starting from a sorted list of consecutive days, each day like this: [year week-of-year date],
  create a list of days like this: [date [year week-of-year absolute-week].
  Absolute week starts with 1 at the beginning and
  increases by one every week."
  [all-weeks]
  (loop [f      (first all-weeks)
         r      (rest all-weeks)
         result (list)]
    (if f
      ; l- last in the sense of 'previous entry'
      ; c- current
      (let [l-week-of-year (get (second (first result)) 2)
            [c-date c-year c-week-of-year ] f
            week-jump  (not= l-week-of-year c-week-of-year)
            l-abs-week (first (second (first result)))
            l-abs-week (if l-abs-week l-abs-week 0) ; init and start with jump
            c-abs-week (if week-jump (inc l-abs-week) l-abs-week)]
        (recur (first r)
               (rest r)
               (conj result [c-date [c-abs-week c-year c-week-of-year]])))
      result)))

(defn list-of-all-days []
  (take (+ (* 365 30) 4)
        (jt/iterate
          jt/plus
          (jt/local-date 2010 1 4) ;; here, calendar week 1 starts.
          (jt/days 1))))

(defn- init-absolut-weeks
  "Creates map with
  key = 'local date' and
  value = [absolute-week year week-of-year]"
  []
  (let [all-days-with-week (map (fn [d] (concat [d] (date-infos-as-vec d)))
                                (list-of-all-days))]
    (into
      (sorted-map)
      (calc-abs-weeks all-days-with-week))))

;; lookup-table
(def abs-week-map (time (init-absolut-weeks)))


(defn get-abs-week
  "Delivers an absolute week number that is linear over all years.
  Starting with 2010-01-04, which is the start of week 1 in that year.
  Ending with 2039-12-31. Before and after that dates, nil results
  will be returned."
  [local-date]
  (assert inst? local-date)
  (abs-week-map local-date))

(defn get-abs-current-week []
  (get-abs-week (jt/local-date)))
