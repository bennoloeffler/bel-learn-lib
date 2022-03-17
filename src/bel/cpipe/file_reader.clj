(ns bel.cpipe.file-reader
  (:require [clojure.string :as str]
            [clojure.test :refer [is deftest]]
            [erdos.assert :as pa]
            [java-time :as jt]
            [bel.util.parse-file :refer :all]
            [bel.util.conversion :refer :all])
  (:import (java.time LocalDate LocalTime)))

;(use 'hashp.core)
;(use 'debux.core)



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; model-specific (task, delivery-date, ...
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ct
  "create task with unnamed args"
  [project start end resource capacity comment]
  {:project  project :start start :end end
   :resource resource :capacity capacity :comment comment})


(defn parse-text-tasks
  "make one string line a task"
  [text]
  (let [result (parse-text
                 text
                 [String LocalDate LocalDate String Double String]
                 [:project :start :end :resource :capacity :comment])]
    result))

(comment
  (pa/assert (=
               (parse-text-tasks
                 "proj1  22.3.2016 24.6.2017 res1   22.0      comment-it-1\n\n
                 proj2  22.3.2017 24.6.2018 res1   23      comment-it-2\n
                 ")
               [(ct "proj1" (d "22.3.2016") (d "24.6.2017") "res1" (i "22") "comment-it-1")
                (ct "proj2" (d "22.3.2017") (d "24.6.2018") "res1" (i "23") "comment-it-2")])))

(defn parse-text-ips
  "make the first number the :max-ips and the rest :project-ips
   make one string line a ip per project"
  [text]
  (let [lines             (separate-lines text)
        max-ip-line       (first lines)
        projects-ip-lines (rest lines)
        projects-ips      (parse-text
                            (str/join "\n" projects-ip-lines)
                            [String LocalDate LocalDate Long]
                            [:project :start :end :capacity])
        max-ips            (l max-ip-line)]
    {:max-ips max-ips :project-ips projects-ips}))


(defn parse-file-tasks [file-str]
  (-> file-str
      slurp
      parse-text-tasks))

(defn parse-file-ips [file-str]
  (-> file-str
      slurp
      parse-text-ips))
(comment
  (parse-file-ips "cpipe-test-files/bsp-03-tasks-kapa-und-ip/Integrations-Phasen.txt"))

(comment
  (parse-file-tasks "cpipe-test-files/Projekt-Start-Ende-Abt-Kapa.txt")
  (parse-file-tasks "cpipe-test-files/bsp-01-nur-tasks/Projekt-Start-End-Abt-Kapa.txt"))


;;-------------
(comment (parse-text-tasks
           "proj1  22.3.2016 24.6.2017 res1   x      comment-it-1\n\n
  proj2  22.3.2017 24.6.2018 res1   23      comment-it-2\n
  "))