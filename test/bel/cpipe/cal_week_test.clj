(ns bel.cpipe.cal-week-test
  (:require [clojure.test :refer :all]
            [java-time :as jt])
  (:require [bel.cpipe.cal-week :refer [get-abs-week]]))

(deftest get-abs-week-test
  (let [[abs-week year week-of-year] (get-abs-week (jt/local-date 2022 1 1))]
     (is (= 626 abs-week))
     (is (= 2022 year))
     (is (= 52 week-of-year))))

(deftest get-abs-week-test-before-range
  (let [[abs-week year week-of-year] (get-abs-week (jt/local-date 2010 1 3))]
    (is (nil? abs-week))
    (is (nil? year))
    (is (nil? week-of-year))))

(deftest get-abs-week-test-in-range-start
  (let [[abs-week year week-of-year] (get-abs-week (jt/local-date 2010 1 4))]
    (is (= 1 abs-week))
    (is (= 1 week-of-year))))

(deftest get-abs-week-test-in-range-end
  (let [[abs-week year week-of-year] (get-abs-week (jt/local-date 2039 12 31))]
    (is (= 1565 abs-week))
    (is (= 52 week-of-year))))

(deftest get-abs-week-test-after-range
  (let [[abs-week year week-of-year] (get-abs-week (jt/local-date 2040 1 1))]
    (is (nil? abs-week))
    (is (nil? year))
    (is (nil? week-of-year))))