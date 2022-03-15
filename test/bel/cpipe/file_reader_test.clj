(ns bel.cpipe.file-reader-test
  (:require [clojure.test :refer :all])
  (:require [bel.cpipe.file-reader :refer :all]
            [erdos.assert :as pa]
            [java-time :as jt])
  (:import (java.time LocalDate)))


(deftest split-line-test
  (is (= ["sdf" "lsdf" "sfdgsd"] (split-line " sdf lsdf    sfdgsd   "))))

(deftest map-line-test
  (is (= (map-line ["axx" "bx" "cxxxx-xx"] [:aa :bb :cc])
         {:aa "axx", :bb "bx", :cc "cxxxx-xx"})))

(deftest separate-lines-test
  (is (= (separate-lines "") []))
  (is (= (separate-lines "\n1a b\n \n  2cde fg\n \n ") ["1a b" "2cde fg"])))

(deftest parse-type-test
  (is (= (jt/local-date 2022 5 17) (parse-type "17.5.2022" LocalDate))))

(deftest parse-typed-line-test
  (is (= ["str", 17, 19.0] (parse-typed-line ["str" "17" "19"] [String Long Double]))))


(deftest parse-text-tasks-test-hangs-small
  #_(is (=
          [{:d (d "22.3.2016") :need 22.0}]
          [{:d (d "22.3.2016") :need 22}])))

(deftest parse-text-tasks-test
  (is (=
        (parse-text-tasks
          "proj1  22.3.2016 24.6.2017 res1   22      comment-it-1\n   \n
          proj2  22.3.2017 24.6.2018 res1   23      comment-it-2\n")
        [(ct "proj1" (d "22.3.2016") (d "24.6.2017") "res1" 22.0 "comment-it-1")
         (ct "proj2" (d "22.3.2017") (d "24.6.2018") "res1" 23.0 "comment-it-2")])))

(deftest parse-file-tasks-test
  (let [data (vec (parse-file-tasks "cpipe-test-files/Projekt-Start-Ende-Abt-Kapa.txt"))]
    (is (= data
           [(ct "proj1" (d "22.3.2016") (d "24.6.2017") "res1" 22.0 "comment-it-1")
            (ct "proj2" (d "22.3.2017") (d "24.6.2018") "res1" 23.0 "comment-it-2")]))))


(deftest parse-file-tasks-test-many
  (let [data (vec (parse-file-tasks "cpipe-test-files/bsp-01-nur-tasks/Projekt-Start-End-Abt-Kapa.txt"))]
    (is (= 12 (count data)))))

(deftest fail-test
  (is (thrown? ArithmeticException (/ 1 0))))


(deftest parse-file-tasks-fail-test
  (is (thrown?
        Exception
        (parse-text-tasks
          "proj1  22.3.2016 24.6.2017 res1   x      comment-it-1\n   \n
          proj2  22.3.2017 24.6.2018 res1   23      comment-it-2\n"))))


(comment
  (parse-text-tasks
    "proj1  22.3.2016 24.6.2017 res1   x      comment-it-1\n   \n
    proj2  22.3.2017 24.6.2018 res1   23      comment-it-2\n"))



