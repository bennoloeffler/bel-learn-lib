(ns bel.util.parse-file-test
  (:require [clojure.test :refer :all]
            [bel.util.parse-file :refer :all]
            [java-time :as jt])
  (:import (java.time LocalDate LocalTime)))


(deftest split-line-test
  (is (= ["sdf" "lsdf" "sfdgsd"] (split-line " sdf lsdf    sfdgsd   "))))

(deftest map-line-test
  (is (= (map-line ["axx" "bx" "cxxxx-xx"] [:aa :bb :cc])
         {:aa "axx", :bb "bx", :cc "cxxxx-xx"})))

(deftest separate-lines-test
  (is (= (separate-lines "") []))
  (is (= (separate-lines "\n1a b\n \n  2cde fg\n \n ") ["1a b" "2cde fg"])))

(deftest parse-type-test
  (is (= (jt/local-date 2022 5 17) (parse-type "17.5.2022" LocalDate)))
  (is (thrown? Exception (parse-type "17.5.2022" LocalTime)))
  (is (thrown? Exception (parse-type "x" Double))))

(deftest parse-typed-line-test
  (is (= ["str", 17, 19.0] (parse-typed-line ["str" "17" "19"] [String Long Double]))))

