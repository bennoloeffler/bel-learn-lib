(ns bel-learn-chapters.x-170-missionary-rx-test
  (:require [clojure.test :refer :all]
            [bel-learn-chapters.x-170-missionary-rx :refer :all]
            [missionary.core :as m]))

;; TODO learn this https://github.com/leonoel/missionary

(deftest missionary-example-test
  (let [result   (atom [])
        add-r    #(swap! result conj %)
        input    (atom 2)
        main     (m/reactor
                   (let [>x (m/signal! (m/watch input)) ; continuous signal reflecting atom state
                         >y (m/signal! (m/latest + >x >x))] ; derived computation, diamond shape
                     (m/stream! (m/ap (add-r (m/?< >y))))))
        disposer (main #(add-r [:success %])  #(add-r [:crash %]))] ; callbacks on success or failure
    (is (= [4] @result)) ; the 4 ( + 2 2)
    (doseq [n (range  7)]
      (swap! input inc))
    (is (= [4 6 8 10 12 14 16 18] @result)) ;  (...(+ 2 (+ 2 ( + 2 2)...) (m/latest + >x >x)
    (disposer)
    (is (= 9 (count @result))))) ; original 4 + 7 times + success = 9 entries


(deftest missionary-example-fail-test
  (let [result   (atom [])
        add-r    #(swap! result conj %)
        input    (atom 2)
        main     (m/reactor
                   (let [>x (m/signal! (m/watch input)) ; continuous signal reflecting atom state
                         >y (m/signal! (m/latest + >x >x (/ (- >x 16))))] ; derived computation, diamond shape
                     (m/stream! (m/ap (add-r (m/?< >y))))))
        disposer (main #(add-r [:success %])  #(add-r [:crash %]))]
    ;(println (m/reactor-call main))
    (swap! input inc)
    (swap! input inc)
    (swap! input inc)
    (swap! input inc)
    (swap! input inc)
    (swap! input inc)
    (swap! input inc)
    (disposer)
    (is (= :crash (ffirst @result)))
    (println @result)))

(deftest missionary-example-async
  (is (= 1 0)))

(deftest missionary-example-parallel-heavy-load
  (is (= 1 0)))

(deftest missionary-example-parallel-heavy-load
  (is (= 1 0)))


