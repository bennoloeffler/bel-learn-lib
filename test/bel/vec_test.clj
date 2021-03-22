(ns bel.vec-test
  (:require [clojure.test :refer :all])
  (:require [bel.vec :refer :all])
  (:import (bel.vec V))
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


(deftest make-rand-vec-test
  (testing "x y random creation"
    (dotimes [n 10]
      (let [td (v n n)]
        ;(Thread/sleep 1)
        ;(println n)
        (is (<= 0 (:x td) n))
        (is (<= 0 (:y td) n)))))
  (testing "from to random creation"
    (let [td (rand-v 1 2 3 4)]
      (is (<= 1 (:x td) 2))
      (is (<= 2 (:y td) 3)))
    (let [td (rand-v -1 1 -2 0)]
      (is (<= -1 (:x td) 0))
      (is (<= -2 (:y td) -1) "should be inside"))))


(deftest distance-test
  (testing "zero"
    (let [v1 (v 0 0)
          v2 (v 0 0)]
      (is (== 0 (distance v1 v2)))))
  (testing "pythagoras"
    (let [v1 (v 0 4)
          v2 (v 3 0)]
      (is (== 5 (distance v1 v2))))))

(comment  
  (run-tests 'bel.vec-test))
