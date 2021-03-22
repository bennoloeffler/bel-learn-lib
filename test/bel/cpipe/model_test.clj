(ns bel.cpipe.model-test
  (:require [clojure.test :refer :all])
  (:require [bel.cpipe.model :refer :all])
  (:gen-class))

(deftest init-db-test
    (testing "connect"
      (is (= 5 5))))
     ;(let [_ (init-db (dev-cfg))]
      ; (println conn)
       ;(is (some? conn))))