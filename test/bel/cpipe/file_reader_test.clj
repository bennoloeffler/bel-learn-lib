(ns bel.cpipe.file-reader-test
  (:require [clojure.test :refer :all])
  (:require [bel.cpipe.file-reader :refer :all]
            [erdos.assert :as pa]
            [java-time :as jt]
            [bel.util.conversion :refer :all])
  (:import (java.time LocalDate LocalTime)))


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

(deftest parse-file-tasks-test-many-many
  (let [data (vec (parse-file-tasks "cpipe-test-files/bsp-05-riesen-datensatz/Projekt-Start-End-Abt-Kapa.txt"))]
    (is (= 5810 (count data)))))

;; the "lazy exception" is not caught by "thrown?"
;; see: https://souenzzo.com.br/reading-and-understanding-clojure-errors.html
;; see: https://www.reddit.com/r/Clojure/comments/a77ja7/clojure_110_error_messages/
;; BECAUSE: it is not evaluated!
;(deftest parse-file-tasks-fail-test
;  (is (thrown?
;        Exception
;        (parse-text-tasks
;          "proj1  22.3.2016 24.6.2017 res1   x    x  comment-it-1\n   \n
;          proj2  22.3.2017 24.6.2018 res1   23      comment-it-2\n"))))
(deftest parse-file-tasks-fail-test
  (is (thrown? Exception
         (vec (parse-text-tasks
                 "proj1  22.3.2016 24.6.2017 res1   x       comment-it-1\n   \n
                      proj2  22.3.2017 24.6.2018 res1   23      comment-it-2\n")))))

(deftest parse-file-ips-test
 (let [expected {:max-ips 2
                 :project-ips
                 [{:end      (d "24.6.2018")
                   :project  "proj2"
                   :capacity 1
                   :start    (d "22.3.2017")}]}
       result (parse-text-ips
                "2\n   \n
                    proj2  22.3.2017 24.6.2018 1 \n")]

  (is (= result expected))))


(deftest parse-file-ips-fail-test
  (is (thrown? Exception
               (vec (parse-text-tasks
                            "2\n   \n
                                 proj2  22.x3.2017 24.6.2018 1 \n")))))






