(ns bel-learn-chapters.x-140-reducers
  [:require [clojure.core.reducers :as r]])


;; https://clojure.org/reference/reducers

(time (->> (range 10000000)
           (map inc)
           (map #(* % %))
           (map str)
           (map last)
           (map long)
           (reduce +)))

(time (->> (range  10000000)
           (r/map inc)
           (r/map #(* % %))
           (r/map str)
           (r/map last)
           (r/map long)
           (r/fold +)))
