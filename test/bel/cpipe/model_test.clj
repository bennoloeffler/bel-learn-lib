(ns bel.cpipe.model-test
  (:require [clojure.test :refer :all]
            [bel.cpipe.model :refer :all]
            [java-time :as jt]
            [datahike.core :as d]))


#_(deftest init-db-test
      (testing "connect"
        (let [conn (init-db (dev-cfg) :delete-db)]
          (println conn)
          (is (some? conn)))))


#_(deftest create-project-test
    (let [conn (init-db (dev-cfg) :delete-db)
          d (clojure.instant/read-instant-date "2017-08-23T10:22:22")]
      (testing "create-it"
          (create-project! conn "p1" d)
          (let [p (d/pull @conn '[*] [:project/name "p1"])]
            (is (= "p1" (:project/name p)))
            (is (= d (:project/delivery-date p)))))
      (testing "add-tasks"
          (add-task! conn "p1" d d 4.7 "something")
          (add-task! conn "p1" d d 4.8 "something2")
          (let [p (d/pull @conn '[*] [:project/name "p1"])]
            (is (= 2 (count (:project/tasks p))))))))
          ;(is (= [#:db{:id 9} #:db{:id 10}] (:project/tasks p)))))))
