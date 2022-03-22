(ns bel-learn-chapters.x-140-reducers
  [:require [clojure.core.reducers :as r]])


;; https://clojure.org/reference/reducers
; https://www.braveclojure.com/quests/reducers/know-your-reducers/

(comment
  (def l (range 10000000))
  (def v (vec l))

  (defn transform [col]
    (->> col
         (map inc)
         (map #(* % %))
         (map inc)
         (map #(/ % 3.0))
         (map str)
         (map last)
         (map long)
         (reduce +)))

  (defn transform-with-red [col]
    (->> col
         (r/map inc)
         (r/map #(* % %))
         (r/map inc)
         (r/map #(/ % 3.0))
         (r/map str)
         (r/map last)
         (r/map long)
         (r/fold +)))

  (time (transform l)) ; 16 secs (no combined comp, no parallel fold
  (time (transform v)) ; 13 secs (vec)

  (time (transform-with-red l)) ; 9 secs list! (no intermediate
  (time (transform-with-red v))) ; 4 (no intermediate col, parallel)





