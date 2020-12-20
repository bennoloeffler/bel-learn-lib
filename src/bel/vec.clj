(ns bel.vec)

(defrecord V [^double x ^double y])

; TODO multimethod for Vec Vec version
(defn make-vec
  "create point or vector :x :y.
  Either from 0/0 to x/y or from
  from-x/from-y to to-x/to-y"
  ([x y]
   (V. x y))
  ([from-x from-y to-x to-y]
   (V. (- to-x from-x) (- to-y from-y))))
;([^Vec from ^Vec to] (Vec. (- (:x to) (:x from)) (- (:y to) (:y from)))))

(defn make-rand-vec
  "randomly create vector in the given range"
  ([range-x range-y]
   (V. (rand-int range-x) (rand-int range-y)))
  ([from-x to-x from-y to-y]
   (V. (+ (rand-int (- to-x from-x))
          from-x)
       (+ (rand-int (- to-y from-y))
          from-y))))

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

(defn in-area?
  [x-from y-from x-to y-to v]
  (cond
    (< (:x v) x-from) false
    (> (:x v) x-to) false
    (< (:y v) y-from) false
    (> (:y v) y-to) false
    :else true))

(defn rand-direction
  "changes the direction randomly without changing the length"
  [v]
  (let [l (len v)
        u (unity (make-rand-vec 1000 1000))]
    (mult-vec u l)))

(def p1 (make-vec 0 0))
(def p2 (make-vec 3 4))
(def p3 (make-vec 1 1))
(def d (distance p3 p2))
(def v1 (make-vec 1 1 3 3))
(def v2 (add-vec v1 v1))
(def v3 (mult-vec v2 3))
(def u (unity v3))
(def ou (rand-direction u))
(distance p3 p2)
(def l (len u))
(println (in-area? 0 0 10 10 (V. -1 0)))
(println u)
(println (len? p1))
