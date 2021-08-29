(ns bel-learn-chapters.09-tests
  (:require [clojure.test :refer :all]
            [bel-learn-lib.core :refer :all]
            [mate-clj.core :refer :all]
            [erdos.assert :as ea] ;:refer :all] ; overwrite assert
            [expectations.clojure.test :refer [defexpect expect expecting more more-> more-of]]))
; http://www.futurile.net/2020/07/14/clojure-testing-with-clojure-test-and-expectations/
; https://github.com/clojure-expectations/clojure-test

(deftest run-readable-test
  (testing "is it human readable"
    (is (= ["val" 51 {:key {:deep-key "dv"}}] ["val" 51 {:key {:deep-key "dv"}}]))))

;;
;; typically the project-layout
;; is:
;
;├───doc
;├───resources
;├───src
;│   └───bel_learn_lib
;├───target
;│   └───...
;└───test
;    └───bel_learn_lib


;;
;; Key bindings should be
;;

;; CTRL-SHIFT-T (Toggle between test and test subject)
;; CTRL-ALT-T (run all tests in current namespace)
;; CTRL-T (run only the current test)
;; CTRL-WIN-T (run last test again)

(deftest partition-by-nums-test
  (testing "empty"
    (is (= [] (partition-by-nums [] [])))
    (is (= [] (partition-by-nums [1 2] [])))
    (ea/is (= [] (partition-by-nums [] [1 2]))))
  (testing "fit"
    (is (= [[1 2 3] [4 5]] (partition-by-nums [3 2] [1 2 3 4 5])))
    (is (= [[7]] (partition-by-nums [1] [7]))))
  (testing "coll bigger"
    (is (= [[1 2 3] [4 5]] (partition-by-nums [3 2] [1 2 3 4 5 6 7]))))
  (testing "coll smaller"
    (is (= [[1 2 3] [4]] (partition-by-nums [3 2] [1 2 3 4]))))
  (testing "with zeros"
    (is (= [[1 2 3] [] [] [4 5]] (partition-by-nums [3 0 0 2] [1 2 3 4 5])))))

(defn ex-test [a b]
  (throw (RuntimeException. "wrong value")))

(defn div-test [a b]
  (/ a b))


;; set breakpoint to exception? CTRL-SHIFT-8
(deftest test-exception
  (testing "general"
    (is (thrown? Throwable
                 (div-test 5 0))))
  (testing "type"
    (is (thrown? ArithmeticException
                          (div-test 5 0))))
  (testing "msg"
    (is (thrown-with-msg? ArithmeticException
                          #"Divide by zero"
                          (div-test 5 0)))
    (is (thrown-with-msg? Throwable #"wrong.*"
                          (ex-test 7 0)))))
(comment
  (test-exception))

(defn number [num]
  "zero")
;; context-menu -> add repl command (edit repl command)
;; put that in the same file and bind it to CTRL-WIN-ALT T
;; :require [clojure.test :refer [is run-tests run-all-tests]]
(defn call-current-tests []
  (println "BELs current tests started")
  (is (= "zero" (number 0))) ;quick-test
  (is (= "one hundred" (number 100))) ; quick-test
  (clojure.test/run-tests 'bel-learn-chapters.09-tests))

(comment
  (run-all-tests #"bel.*test|clj-app.*test.*"))

(defn greeting
  ([] (str "Hello, World!"))
  ([x] (str "Hello, " x "!"))
  ([x y] (str x ", " y "!")))

;; For quick-testing
(assert (= "Hello, World!" (greeting)))
(assert (= "Hello, Power-Assert!" (greeting "Power-Assert"))) ;; power-assert!
(assert (= "Good morning, Clojure!" (greeting "Good morning" "Clojure")))

;(defexpect string-user-name-test
;  (expect "John Smith" (str "John" "Williams")))

;;
;; ---------- continous testing ----------
;;

; metrosin bat-test https://github.com/metosin/bat-test
; lein bat-test auto
; lein bat-test cloverage
; http://www.futurile.net/2020/07/14/clojure-testing-with-clojure-test-and-expectations/

; test-refresh https://github.com/jakemcc/lein-test-refresh
; lein test-refresh