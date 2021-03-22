(ns bel.cpipe.ui-test
  (:require [clojure.test :refer :all])
  (:require [bel.cpipe.ui :refer :all]))

(deftest set-frame-content!-test
  (testing "9"
    (is (= 999 999)))
  (testing "8"
    (is (= 888 888)))
  (testing "test the 7"
    (is (= 7 7))
    (is (= 77 77)))
  (testing "structure"
    (is (= {:a 4 :b {:sub1 "abc" :sub2 "def"} :c 12.6} {:a 4 :b {:sub1 "abc" :sub2 "def"} :c 12.6}))))
