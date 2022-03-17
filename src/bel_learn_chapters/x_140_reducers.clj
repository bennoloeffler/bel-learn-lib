(ns bel-learn-chapters.x-140-reducers
  [:require [clojure.core.reducers :as r]])


;; https://clojure.org/reference/reducers

(comment
  (def v (vec (range 10000000)))
  (time (->> v
             (map inc)
             (map #(* % %))
             (map inc)
             (map #(/ % 3.0))
             ;(map str)
             ;(map last)
             ;(map long)
             (reduce +)))

  (time (->> v
             (r/map inc)
             (r/map #(* % %))
             (r/map inc)
             (r/map #(/ % 3.0))
             ;(r/map str)
             ;(r/map last)
             ;(r/map long)
             (r/fold +))))
