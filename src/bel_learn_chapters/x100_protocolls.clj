(ns bel-learn-chapters.x100-protocolls
  (:import (java.lang String)))

; https://www.braveclojure.com/multimethods-records-protocols/
; see Book pdf Pragmatic.Clojure.Applied.From.Practice.to.Practitioner

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
                     :name      "Rachel from next door"})

(full-moon-behavior {:name      "Andy the baker"
                     :were-type :simmons})

(full-moon-behavior {:name "Benno the consultant"})
(full-moon-behavior {:name      "Sabine the mother"
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
(new Point 1 2)

; extend an existing protocol
(extend-type Point Psychodynamics
  (thoughts [this] (str "I'm a Point with thoughts..." (:x this) (:y this)))
  (feelings-about
    ([this] (str "points dont feel. they just are " (:x this) ":" (:y this)))
    ([this other] (str "no feelings about other -> " other))))

(thoughts (Point. 2 3))
(feelings-about (Point. 2 3) :other)
(feelings-about (Point. 2 3))


;; -----------------------
;; combine: define record and protocol implementation
;; -----------------------


; define a record and implement a protocol
(defrecord WereWolf [name title]
  Psychodynamics
  (thoughts [x] (str "I'm a WereWolf: " (:title x) " WERE-" (:name x)))
  ; multi arity are separately defined!
  (feelings-about [x] (str "Im a a bloody " (:name x)))
  (feelings-about [x y] (str "Im a happy biting -> " y)))

(def w (->WereWolf "B" "Dr."))
(thoughts w)
(feelings-about w 5)


; tutorial video https://www.youtube.com/watch?v=xpH6RGjZwNg

(comment
  (def jeep-wrangler {:make "Jeep" :model "Wrangler"})

  (defrecord CarModel [make model])
  (def fiat-500 (->CarModel "Fiat" "500"))
  (def ford-focus (map->CarModel {:make "Ford" :model "Focus"}))
  (:make ford-focus)
  (:model fiat-500)

  (defprotocol ProductInfo
    (title [this])
    (description [this description]))

  ;; if you are able to implement protocols / interfaces
  ;; directly: do it
  (defrecord CarModel [make model]
    ProductInfo
    (title [this] (str "This is a " make " " model))
    (description [this description] (str "The " make " " model " is " description))
    Object
    (toString [this] (str "STRING: " (title this))))

  (def fiat-500 (->CarModel "Fiat" "500"))
  (str fiat-500)
  (title fiat-500)
  (description fiat-500 "a great, supersmall car")
  nil)


(comment
  ;; imagine, HomeProduct would be defined in
  ;; a library package...
  (defrecord HomeProduct [product-name price])

  ; you may create it and access it like a map
  (def toaster (->HomeProduct "Toaster" 45.99))
  (:price toaster)
  (str toaster) ; but this does work strange
  ; and what about ProductInfo? How could
  ; you make HomeProducts use the same
  ; abstract protocol like all your other product types?
  nil)

(comment
  ;; use extend-protocol, if you have no access to
  ;; the base like Object or HomeProduct

  (defprotocol PAppendable
    (append [this to-append]))

  (extend-protocol PAppendable
    String
    (append [this to-append] (str this ", " to-append)))

  ; makes append applicable to String
  (append "my-str" :something)


  (extend-protocol ProductInfo
    HomeProduct
    (title [this] (str "This Homeprodukt is a " (:product-name this)))
    (description [this description] (str "The homeproduct " (:product-name this) " is " description)))

  (def toaster (->HomeProduct "Toaster" 45.99))
  (title toaster)
  (description toaster "a blinking, warm product")

  (defmethod print-method HomeProduct [hp, w] ; Overload the printer
    (print-method (str "PR-STRING: " (title hp)) w))

  (type toaster)
  (println toaster)
  (str toaster) ; don't know how to overload toString
  ; regarding toString, read this:
  ; https://stackoverflow.com/questions/5306015/equivalent-of-javas-tostring-for-clojure-functions
  nil)