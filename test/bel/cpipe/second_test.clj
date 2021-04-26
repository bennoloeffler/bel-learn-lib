(ns bel.cpipe.second-test
  (:require [clojure.test :refer :all]))

(deftest equal-test
    (testing "equal"
     (is (= [1 1] [1 1]))))
