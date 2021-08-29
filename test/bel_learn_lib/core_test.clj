(ns bel-learn-lib.core-test
  (:require [clojure.test :refer :all]
            [bel-learn-lib.core :refer :all]))

(deftest partition-by-nums-test
  (testing "empty"
    (is (= [] (partition-by-nums [] [])))
    (is (= [] (partition-by-nums [1 2] [])))
    (is (= [] (partition-by-nums [] [1 2]))))
  (testing "fit"
    (is (= [[1 2 3] [4 5]] (partition-by-nums [3 2] [1 2 3 4 5])))
    (is (= [[7]] (partition-by-nums [1] [7]))))
  (testing "coll bigger"
    (is (= [[1 2 3] [4 5]] (partition-by-nums [3 2] [1 2 3 4 5 6 7]))))
  (testing "coll smaller"
    (is (= [[1 2 3] [4]] (partition-by-nums [3 2] [1 2 3 4]))))
  (testing "with zeros"
    (is (= [[1 2 3] [] [] [4 5]] (partition-by-nums [3 0 0 2] [1 2 3 4 5])))))

(deftest get-digits-test
  (testing "nil"
    (is (= [] (get-digits nil))))
  (testing "zero"
    (is (= [0] (get-digits 0)))
    (is (= [1 0] (get-digits 10)))))

;; make a difference in data and see test runner output...
(deftest run-readable-test
  (testing "is it human readable"
    (is (= ["val" 51 {:key {:deep-key "dv"}}] ["val" 51 {:key {:deep-key "dv"}}]))))


;(run-all-tests)
