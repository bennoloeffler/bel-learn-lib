(ns bel.vec)


;; definition of 2D-vector x y
(defrecord V [^double x ^double y])

;; multiples of pi
(def pi Math/PI)
(def pi*2 (* 2 pi))
(def pi-half (/ pi 2))
(def pi-3half (* 3 pi-half))

(defmulti v
  "create vector from either
   0/0 to x/y
   or
   pos1 to pos2.
   Given as x1 y1 x2 y2
   or as vector v1 v2"
  (fn ([x y & xs]
       (mapv class (into [x y] xs)))))

(defmethod v [Number Number]
  [x y & xs]
 "create vector from 0/0 to x/y"
  (V. x y))

(defmethod v [Number Number Number Number]
  [x1 y1 & xy2]
 "create difference vector from x1 y1 to x2 y2"
  (V. (- (first xy2) x1) (- (second xy2) y1)))

(defmethod v [bel.vec.V bel.vec.V]
 [v1 v2 & xs]
 "create difference vector v1 to v2"
 (V. (- (:x v2) (:x v1)) (- (:y v2) (:y v1))))

(defn rand-v
  "randomly create vector in the given range"
  ([range-x range-y]
   (V. (rand-int range-x) (rand-int range-y)))
  ([from-x to-x from-y to-y]
   (V. (+ (rand-int (- to-x from-x))
          from-x)
       (+ (rand-int (- to-y from-y))
          from-y))))

;;;; all the simple calculations

(defn distance
  "the distance from one point :x :y to another"
  [from-v to-v]
  (Math/sqrt
    (+
      (Math/pow (- (:x to-v) (:x from-v)) 2)
      (Math/pow (- (:y to-v) (:y from-v)) 2))))

(defn add-vec
  "add two vectors"
  [v1 v2]
  {:pre [v1 v2] :post [%]}
  (V.
    (+ (:x v1) (:x v2))
    (+ (:y v1) (:y v2))))

(defn mult-vec
  "multiply vector v with num"
  [v num]
  (V.
    (* (:x v) num)
    (* (:y v) num)))

(defn len
  "get the len of a vector :x :y"
  [v]
  (Math/sqrt
    (+
      (Math/pow (:x v) 2)
      (Math/pow (:y v) 2))))

(defn len?
  "is vector longer than zero"
  [v]
  (let [l (len v)]
    (not= l 0.0)))

(defn unity
  "get unity vector (vector of len 1) of v"
  [v]
  {:pre [(len? v)] :post [(len? %)]}
  (let [l (len v)]
    (V.
      (/ (:x v) l)
      (/ (:y v) l))))

(defn unity?
  "is unity vector?"
  [v]
  (let [l (len v)]
    (== l 1.0)))

;;;; everything with angels

(defn pi-to-grad
  "PI => 180°"
  [pi-num]
  (* pi-num (/ 180 Math/PI)))

(defn grad-to-pi
  "180° => PI"
  [alpha]
  (* alpha (/ Math/PI 180)))

(defn rotate
  "rotate around zero point PI = 180° left"
  [v pi-angle]
  (V.
   (- (* (:x v) (Math/cos pi-angle)) (* (:y v) (Math/sin pi-angle)))
   (+ (* (:x v) (Math/sin pi-angle)) (* (:y v) (Math/cos pi-angle)))))

(defn pi-angle
  "get angle of vector: pos x-axis = 0, pos y-axis = PI/2"
  [v]
  (Math/atan2 (:x v) (:y v)))

(defn alpha-angle
  "get angle of vector: pos x-axis = 0, pos y-axis = 90°"
  [v]
  (pi-to-grad (pi-angle v)))

;;;; gaming specific

(defn in-area?
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
  (let [l (len v)
        u (unity (rand-v -1000 1000 -1000 1000))]
    (mult-vec u l)))



(comment

  (in-area? 0 0 1 1 (v 0.0000000 0.9))
  (in-area? 0 0 1 1 (v -0.0000000 1.00001))
  ;(in-area? 2 0 1 1 (v -0.0000000 1.00001))

  (pi-to-grad pi-half)
  (grad-to-pi 180)
  (def r1 (v 2 2))
  (alpha-angle r1)
  (rotate r1 (/ Math/PI 2))

  (def p1 (v 0 0))
  (def p2 (v 3 4))
  (def vv (v p2 p1))
  (def p3 (v 2 1))
  (def d (distance p3 p2))
  (def v1 (v 1 1 3 3))
  (def v2 (add-vec v1 v1))
  (def v3 (mult-vec v2 3))
  (def u (unity v3))
  (def ou (rand-direction u))
  (distance p3 p2)
  (def l (len u))
  (println l)
  (println (in-area? 0 0 10 10 (V. -1 0)))
  (println u)
  (println (len? p1)))
