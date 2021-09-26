(ns belib.vec-test
  (:require [clojure.test :refer :all])
  (:require [belib.vec :refer :all])
  (:import (belib.vec V))
  (:gen-class))

(deftest make-vec-test
  (testing "x y creation"
    (is (= (V. 1 2) (v 1 2))))
  (testing "from to creation"
    (is (= (V. 2 3) (v 1 2 3 5))))
  (testing "invalid x y"
    (is (thrown? Exception (v nil 1))))
  (testing "invalid from to"
    (is (thrown? Exception (v 0 1 0 nil)))))


(deftest v-rand-test
  (testing "x y random creation"
    (dotimes [n 10]
      (let [td (v n n)]
        ;(Thread/sleep 1)
        ;(println n)
        (is (<= 0 (:x td) n))
        (is (<= 0 (:y td) n)))))
  (testing "from to random creation"
    (let [td (v-rand 1 2 3 4)]
      (is (<= 1 (:x td) 2))
      (is (<= 2 (:y td) 3)))
    (let [td (v-rand -1 1 -2 0)]
      (is (<= -1 (:x td) 0))
      (is (<= -2 (:y td) -1) "should be inside"))))


(deftest v-distance-test
  (testing "zero"
    (let [v1 (v 0 0)
          v2 (v 0 0)]
      (is (== 0 (v-distance v1 v2)))))
  (testing "pythagoras"
    (let [v1 (v 0 4)
          v2 (v 3 0)]
      (is (== 5 (v-distance v1 v2))))))

(comment
  (run-tests 'bel.vec-test 'bel.))

(deftest v-unity?-test
  (testing "unity"
    (let [sqrt2len (v 1 1)
          is-unity (v (/ 1 (Math/sqrt 2)) (/ 1 (Math/sqrt 2)))
          is-calc-unity (v-unity (v 13 47))]
      (is (not (v-unity? sqrt2len)))
      (is (v-unity? is-unity))
      (is (v-unity? is-calc-unity)))))

(deftest v?-test
  (is (not (v? 9)))
  (is (v? (V. 0 0))))

(deftest v-test
  (is (= (V. 16 1) (v 1 2 17 3))))

(deftest v-minus-test
  (is (= (V. -16 -1) (v-minus (v 1 2 17 3))))
  (is (= (V. -17 -3) (v-minus (v 17 3)))))

