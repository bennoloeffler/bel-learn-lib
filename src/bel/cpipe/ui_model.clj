(ns bel.cpipe.ui-model
  (:require [bel.cpipe.week-cal :as w]
            [java-time :as jt]))

(def ui-model {"p1" {:start-week 1234 :end-week 1254}
               "p2" {:start-week 1242 :end-week 1256}})

(defn abs-weeks-from-project [project])

(jt/min (jt/local-date 2015 2 3) (jt/local-date 2015 1 3))