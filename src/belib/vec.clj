(ns belib.vec
  (:require [tupelo.core :refer [rel=]]))


;;
;; ---------- definition of 2D-vector x y ----------
;;
(defrecord V [^double x ^double y])


;;
;; ----------- multiples of pi -----------
;;
(def pi Math/PI)
(def pi*2 (* 2 pi))
(def pi-half (/ pi 2))
(def pi-3half (* 3 pi-half))

;;
;; ---------- create 2D vector ----------
;;

(defmulti v-create
          "create vector from either
           0/0 to x/y
           or
           pos1 to pos2.
           Given as
           x1 y2 or
           x1 y1 x2 y2
           or as vector
           v1 v2"
          (fn ([x y & xs]
               (mapv class (into [x y] xs)))))

(defmethod v-create [Number Number]
  [x y & _]
  "create vector from 0/0 to x/y"
  (V. x y))

(defmethod v-create [Number Number Number Number]
  [x1 y1 & xy2]
  "create difference vector from x1 y1 to x2 y2"
  (V. (- (first xy2) x1) (- (second xy2) y1)))

(defmethod v-create [V V]
  [v1 v2 & _]
  "create difference vector v1 to v2"
  (V. (- (:x v2) (:x v1)) (- (:y v2) (:y v1))))

(def v v-create) ; SHORTCUT for all v-create functions


(defn v-rand
  "randomly create vector in the given range"
  ([range-x range-y]
   (V. (rand-int range-x) (rand-int range-y)))
  ([from-x to-x from-y to-y]
   (V. (+ (rand-int (- to-x from-x))
          from-x)
       (+ (rand-int (- to-y from-y))
          from-y))))

(defn v?
  "is this of type V?"
  [o]
  (= V (type o)))

;;
;; ----------- all the simple calculations -----------
;;

(defn v-distance
  "the distance from one point :x :y to another"
  [from-v to-v]
  (Math/sqrt
    (+
      (Math/pow (- (:x to-v) (:x from-v)) 2)
      (Math/pow (- (:y to-v) (:y from-v)) 2))))


(defn v-minus
  "subtract two vectors (v1 - v2) - or just get negative values of v1"
  ([v1]
   (V. (- (:x v1)) (- (:y v1))))
  ([v1 v2]
   {:pre [v1 v2] :post [%]}
   (V.
     (- (:x v1) (:x v2))
     (- (:y v1) (:y v2)))))


(defn v-add
  "add two vectors"
  [v1 v2]
  {:pre [v1 v2] :post [%]}
  (V.
    (+ (:x v1) (:x v2))
    (+ (:y v1) (:y v2))))

(defn v-mult
  "multiply vector v with num"
  [v num]
  (V.
    (* (:x v) num)
    (* (:y v) num)))

(defn v-len
  "get the len of a vector :x :y"
  [v]
  (Math/sqrt
    (+
      (Math/pow (:x v) 2)
      (Math/pow (:y v) 2))))

(defn v-not-zero?
  "is vector longer than zero"
  [v]
  (let [l (v-len v)]
    (not= l 0.0)))

(defn v-unity
  "get unity vector (vector of len 1) of v"
  [v]
  {:pre [(v-not-zero? v)] :post [(v-not-zero? %)]}
  (let [l (v-len v)]
    (V.
      (/ (:x v) l)
      (/ (:y v) l))))

(defn v-unity?
  "is unity vector?"
  [v]
  (let [l (v-len v)]
    (rel= l 1.0 :digits 12)))

; https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
#_(defn v-intercept
    "intersection point of two lines"
    [v1-from v2-to v3-from v4-to])


;;
;; ----------- everything with angles -----------
;;

(defn v-pi-to-grad
  "PI => 180°"
  [pi-num]
  (* pi-num (/ 180 Math/PI)))

(defn v-grad-to-pi
  "180° => PI"
  [alpha]
  (* alpha (/ Math/PI 180)))

(defn v-rotate
  "rotate around zero point (PI = 180° left)"
  [v pi-angle]
  (V.
    (- (* (:x v) (Math/cos pi-angle)) (* (:y v) (Math/sin pi-angle)))
    (+ (* (:x v) (Math/sin pi-angle)) (* (:y v) (Math/cos pi-angle)))))

(defn v-pi-angle
  "get angle of vector: pos x-axis = 0, pos y-axis = PI/2"
  [v]
  (Math/atan2 (:x v) (:y v)))

(defn v-alpha-angle
  "get angle of vector: pos x-axis = 0, pos y-axis = 90°"
  [v]
  (v-pi-to-grad (v-pi-angle v)))

;;
;; ---------- check if inside something ----------
;;

(defn v-in-area?
  "is v in the box? including the border values!"
  [x-from y-from x-to y-to v]
  {:pre [(< x-from x-to) (< y-from y-to)]}
  (cond
    (<= (:x v) x-from) false
    (>= (:x v) x-to) false
    (<= (:y v) y-from) false
    (>= (:y v) y-to) false
    :else true))

(defn rand-direction
  "changes the direction randomly without changing the length"
  [v]
  (let [l (v-len v)
        u (v-unity (v-rand -1000 1000 -1000 1000))]
    (v-mult u l)))



(comment

  (v-in-area? 0 0 1 1 (v-create 0.0000000 0.9))
  (v-in-area? 0 0 1 1 (v-create -0.0000000 1.00001))
  ;(in-area? 2 0 1 1 (v -0.0000000 1.00001))

  (v-pi-to-grad pi-half)
  (v-grad-to-pi 180)
  (def r1 (v-create 2 2))
  (v-alpha-angle r1)
  (v-rotate r1 (/ Math/PI 2))

  (def p1 (v-create 0 0))
  (def p2 (v-create 3 4))
  (def vv (v-create p2 p1))
  (def p3 (v-create 2 1))
  (def d (v-distance p3 p2))
  (def v1 (v-create 1 1 3 3))
  (def v2 (v-add v1 v1))
  (def v3 (v-mult v2 3))
  (def u (v-unity v3))
  (def ou (rand-direction u))
  (v-distance p3 p2)
  (def l (v-len u))
  (println l)
  (println (v-in-area? 0 0 10 10 (V. -1 0)))
  (println u)
  (println (v-not-zero? p1)))
