(ns bel.util.conversion-test
  (:require [clojure.test :refer :all]
            [bel.util.conversion :refer :all]
            [java-time :as jt]))


(deftest d-test
  (is (= (jt/local-date 2015 1 12) (d "12.1.2015"))))

(deftest parse-double-fail-test
  (is (thrown-with-msg? Exception #"x2.3" (n "x2.3"))))

(deftest parse-long-fail-test
  (is (thrown-with-msg? Exception #"2.3" (l "2.3"))))

(deftest parse-date-fail-test
  (is (thrown-with-msg? Exception #"22.x.2020" (d "22.x.2020"))))

(deftest l-test
  (is (= 15 (l "15"))))
