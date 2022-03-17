(ns bel.util.basics-test
  (:require [clojure.test :refer :all])
  (:require [bel.util.basics :refer [!=]]))

(deftest !=-test
  (is (!= 2 3)))
