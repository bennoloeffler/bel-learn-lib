(ns bel-learn-chapters.40-game
  (:require [bel.vec :refer :all])
  (:import (bel.vec V)))

(defrecord Food [^V pos ^V speed ^double energy ^double poison])

(def world-x 1000)
(def world-y 700)
(def max-speed 20)
(def max-poison 100)
(def max-energy 100)

(def the-state (atom {:food []
                      :man-monster []
                      :auto-monster []}))

(defn make-rand-food []
  (Food. (rand-v world-x world-y)
         (rand-v max-speed max-speed)
         (rand-int max-energy)
         (rand-int max-poison)))

(defn add-one-food [state]
  (let [new-food (make-rand-food)]
    (assoc state :food (conj (:food state) new-food))))

(defn add-food [number state]
  ;(println number)
  (if (<=  number 0)
    state
    (recur (dec number) (add-one-food state))))

(defn move-one-food
  [food]
  {:pre [food (:pos food) (:speed food)]}
  (let [new-pos (add-vec (:pos food) (:speed food))]
    ;(println "--> FOOD: " food)
    (loop [pos new-pos food food]
      (if (in-area? 0 0 world-x world-y pos)
        (Food. pos (:speed food) (:energy food) (:poison food))
        (let [new-speed (rand-direction (:speed food))]
          ;(println " - - - - CHANGED DIRECTION - - - - ")
          ;(println "speed: " new-speed)
          ;(println "pos: " (:pos food))
          (recur
            (add-vec (:pos food) new-speed)
            (assoc food :speed new-speed)))))))


;if out of game --> new vector with same speed that leads back in

(defn move-food [state]
  (loop [old-food (:food state)
         new-food {}]
    ;(println "old-food size: " (count old-food))
    ;(println "new-food size: " (count new-food))
    (if-not (seq old-food)
      (assoc state :food new-food)
      (recur (rest old-food) (conj new-food (move-one-food (first old-food)))))))

(defn move-word
  [state]
  (-> state
      move-food))

;  (move-food (move-monster state))

;(println @the-state)
(swap! the-state (partial add-food 2))
(swap! the-state (partial add-food 2))
;(println @the-state)
(swap! the-state move-food)
;(println @the-state)
nil


;(println (add-food @*state* 10))
