(ns bel.cpipe.week-cal
  (:require [java-time :as jt])
  (:import [java.time.temporal ChronoField  WeekFields]
           [java.time LocalDate]
           [java.util Locale]))
;; a linear week model since 1.1.1970
;; week 1 2 3 4 5
;; mapping to date
;; mapping to year-week

;; the right temporal field to get linear calender weeks
(def temporal-field
  (.weekOfWeekBasedYear (WeekFields/of (Locale/getDefault))))

;; get calender-weeks that have 7 days
(defn calender-week [d]
  (.get d temporal-field))

(defn week-year [d]
  (let [y (.get d ChronoField/YEAR)
        ;m (.get d ChronoField/MONTH_OF_YEAR)
        w (calender-week d)]
        ;d (.get d ChronoField/DAY_OF_WEEK)]
        ;w (if (and (= m 12) (= w 1)))]

    [y w]))

(def abs-weeks {})
#_(defn calc-abs-weeks [all-weeks]
    (reduce (fn [initial val]
              (let [last-week (last (last initial))
                    cur-week (last val)
                    abs-last-week ()]
               (conj initial val) [] all-weeks))))

(defn init-absolut-weeks []
  (let [all-weeks (map (fn [d] (conj (week-year d) d))
                       (take 20
                             (jt/iterate
                               jt/plus
                               (jt/local-date 1970 1 1)
                               (jt/days 1))))]
    all-weeks))

(def test-data-2 (init-absolut-weeks))

(def test-data
  [[ "1970-01-01" [1970 1]]
   [ "1970-01-02" [1970 1]]
   [ "1970-01-03" [1970 1]]
   [ "1970-01-04" [1970 2]]
   [ "1970-01-05" [1970 2]]
   [ "1970-01-06" [1970 2]]
   [ "1970-01-07" [1970 2]]
   [ "1970-01-08" [1970 2]]
   [ "1970-01-09" [1970 2]]
   [ "1970-01-10" [1970 2]]
   [ "1970-01-11" [1970 3]]
   [ "1970-01-12" [1970 3]]
   [ "1970-01-13" [1970 3]]
   [ "1970-01-14" [1970 3]]
   [ "1970-01-15" [1970 3]]
   [ "1970-01-16" [1970 3]]
   [ "1970-01-17" [1970 3]]
   [ "1970-01-18" [1970 4]]
   [ "1970-01-19" [1970 4]]
   [ "1970-01-20" [1970 4]]
   [ "1970-01-21" [1970 1]]
   [ "1970-01-22" [1970 1]]
   [ "1970-01-23" [1970 2]]])

(reduce (fn[cum [year week date]]
          (let [abs (cum)]
           (conj cum [date week year]))) [] test-data)

#_(calc-abs-weeks test-data)