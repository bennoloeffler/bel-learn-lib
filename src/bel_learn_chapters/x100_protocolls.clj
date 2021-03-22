(ns bel-learn-chapters.x100-protocolls
  (:import (java.lang String)))

; https://www.braveclojure.com/multimethods-records-protocols/

(defmulti full-moon-behavior (fn [were-creature] (:were-type were-creature)))
(defmethod full-moon-behavior :wolf
   [were-creature]
   (str (:name were-creature) " will howl and murder"))
(defmethod full-moon-behavior :simmons
   [were-creature]
   (str (:name were-creature) " will encourage people and sweat to the oldies"))
(defmethod full-moon-behavior :default
   [were-creature]
   (str (:name were-creature) " will do NOTHING"))

(full-moon-behavior {:were-type :wolf
                     :name "Rachel from next door"})

(full-moon-behavior {:name "Andy the baker"
                     :were-type :simmons})

(full-moon-behavior {:name "Benno the consultant"})
(full-moon-behavior {:name "Sabine the mother"
                     :were-type :sports-woman})

;; -----------------------
;; multiple dispatch
;; -----------------------

(defmulti types (fn [x y] [(class x) (class y)]))
(defmethod types [String String]
  [x y]
  (str "Two strings! " x " " y))
(defmethod types :default
  [x y]
  (str "NOT Two strings! " x " " y))

(types "a-h" "b-h")
(types :somthing-else "String 2")


;; -----------------------
;; protocols
;; -----------------------

(defprotocol Psychodynamics
  "Plumb the inner depths of your data types"
  (thoughts [x] "The data type's innermost thoughts")
  (feelings-about [x] [x y] "Feelings about self or other"))

; extend existing types
(extend-type String Psychodynamics
 (thoughts [x] (str "I'm a chain of chars... and would like to be uppercase..." x))
 (feelings-about
  ([x] (str "I'm a lonely string..." x))
  ([x y] (str "I'm a happy socializing string: " x " <-> " y))))

(feelings-about "me")
(thoughts "abc")
(feelings-about "me" :another)

; default implementation...
(extend-type Object Psychodynamics
 (thoughts [x] (str "You are looking at me as an object :-( " x))
 (feelings-about
  ([x] (str "object " x))
  ([x y] (str "as object, im not subjective on -> " y))))

(feelings-about :abc "string")


;; -----------------------
;; records
;; -----------------------


; simple record and different ways of creation
(defrecord Point [x y])
(map->Point {:x 3 :y 7})
(->Point 5 6)
(Point. 45 67)

; extend an existing protocol
(extend-type Point Psychodynamics
  (thoughts [x] (str "I'm a Point with thoughts..." (:x x) (:y x)))
  (feelings-about
   ([x] (str "points dont feel. they just are " (:x x) ":" (:y x)))
   ([x y] (str "no feelings about -> " y))))

(thoughts (Point. 2 3))
(feelings-about (Point. 2 3) :other)
(feelings-about (Point. 2 3))


;; -----------------------
;; combine: define record and protocol implementation
;; -----------------------


; define a record and implement a protocol
(defrecord WereWolf [name title] Psychodynamics
  (thoughts [x] (str "I'm a WereWolf: " (:title x) " WERE-" (:name x)))
  ; multi arity are separately defined!
  (feelings-about [x] (str "Im a a bloody " (:name x)))
  (feelings-about [x y] (str "Im a happy biting -> " y)))

(def w ( ->WereWolf "B" "Dr."))
(thoughts w)
(feelings-about w 5)

