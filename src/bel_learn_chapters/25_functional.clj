(ns bel-learn-chapters.25-functional
  (:import [clojure.lang IFn]))


;;----------------------
;; a var holding a value
;;----------------------
(def ten-by-three 10) ; have a var: divide 10 by 3 (function operand1 operand2 ...)
(type 'ten-by-three) ; type of the symbol itself
(type #'ten-by-three) ; type of the var itself
(type ten-by-three) ; type of the resolved value


;;----------------------
;; a anonymous function
;;----------------------
(fn [number] (/ number 3)) ; annonymous function
#(/ % 3) ; the same, but shorter

(type (fn [])) ; type of a anonymous function
(type (fn a-name [])) ; even an anonymous function may have a name - just for debugging, not for calling!


;;----------------------------
;; a function asigned to a var
;;----------------------------
(def third (fn [number] (/ number 3))) ; function with name
(third 13) ; 13/3
(supers (type third)) ; interfaces of function types

; just an abbrevation (macro) for (def fifth (fn [n] (/ n 5)))
(defn fifth
  "Divide operand by 5." ; here you may provide a doc string.
  [n]
  (/ n 5))
(fifth 10)
(supers (type fifth)) ; interfaces of function types
(instance? IFn fifth)
(clojure.repl/doc fifth) ; look for the doc string

;;----------------------------
;; return types
;;----------------------------

; may return different types - depending on parameters
(type (fifth 10.0))
(type (fifth 10000000000000000000000))

;;---------------------------------------------
;; different numbers of parameters (multi-arity
;;---------------------------------------------

(defn half ;multi-arity
  "diviede operand by two - or return 1/2, if no operand" ;docstring
  ([] 1/2) ; first function WITHOUT parameter
  ([x] (/ x 2))) ; second function WITH ONE parameter

(half 5)
(half)

;;----------------------------------------------------------------------
;; applying and composing functions, sometimes partial and complementary
;;----------------------------------------------------------------------


;; apply
;;-----------------------------

; apply makes functions appicable to collections when they
; expect parameters. See documentation. Good examples:
; https://clojuredocs.org/clojure.core/apply
(apply max [1 4 3 7 3])
(apply + #{1 2 3}) ; same as (+ 1 2 3)
(apply + 1 2 '(3 4))  ; equivalent to (apply + '(1 2 3 4))



;; comp - compose
;;-----------------------------

; compose creates a function out of other functions
(def fifth-element (comp first rest rest rest rest))
(println (fifth-element [1 2 3 4 5 6 7]))

; composing is BACKWARDS!
(def negative-ratio-string (comp str - /))
(negative-ratio-string 8 6)

; could change that easily
(defn comp-forward [& params]
  (apply comp (reverse params)))

(def negative-ratio-string-f (comp-forward / - str))
(negative-ratio-string-f 8 6)

; an example of composing a function the functional way
; we would like to have (nth-composed n) to return
; a function that, when called, calles rest n times and
; then returns the first.
(comment
  (rest [1 2 3]) ; rest gives all but the first element
  (rest (rest [1 2 3]))
  (take 3 (repeat rest)) ; creates a list of "rests"
  ((apply comp (take 3 (repeat rest))) ;; this returns a function from the funs in the list
   [1 2 3 4 5 6 7]) ; and calls it on this vector
  (apply comp (cons first (take 6 (repeat rest)))) ; add fun first to the list

  ; create a function
  (defn n-th [n]
    (apply comp
           (cons first
                 (take (dec n) (repeat rest)))))

  (def forth (n-th 4)) ; create a function by calling a function

  (forth [4 3 2 1 0 -1])

  nil)


;; partial
;;-----------------------------


(def p5 (partial + 5))
(p5 8 7)

(def p5-2 #(apply + 5 %&))
(p5-2 8 7)


;; complement
;;-----------------------------

(def not-str? (complement string?))
(not-str? 1)
(not-str? "1")

(let [truthiness (fn [v] v)]
  [((complement truthiness) true)
   ((complement truthiness) 42)
   ((complement truthiness) false)
   ((complement truthiness) nil)])



;;-----------------------------
;; mapping, filtering, reducing
;;-----------------------------

;; mapping with threading macro and intermediate collections - good to debug
(->> '(a B C)
     (map name)
     (map #(.toLowerCase %))
     (map keyword))

;; comp may be used to compose mapping functions
(map (comp
       keyword
       #(.toLowerCase %)
       name)
     '(a B C))

(def to-keyword (comp
                  keyword
                  #(.toLowerCase %)
                  name))

; easy to read, no intermediate collections
(map to-keyword '(a B C))


;;-----------------------
;; mapping with macros
;;-----------------------
(defmacro make-fn [m]
  `(fn [& args#]
     (eval
       (cons '~m args#))))

#_(map or ; does not work
       [false false false true]
       [false false true  true]
       [false true  true  true])

(map (make-fn or)
     [false false false true]
     [false false true  true]
     [false true  true  true])


(map #(or %1 %2 %3) ; or just "by hand"
     [false false false true]
     [false false true  true]
     [false true  true  true])

;;-----------------------------------------
;; tests functions as metadata at functions
;;-----------------------------------------


(defn square
  {:test (fn []
           (assert
             (= (square 2) 4)))}
  [n]
  (* n n))

(comment
  (use '[clojure.test :as t])
  (t/run-tests))


;;-----------------------------------------
;; sorting
;;-----------------------------------------
(comment
  (sort [1 5 7 0 -42 13])
  (sort compare [1 5 7 0 -42 13])
  (sort (comp - compare) [1 5 7 0 -42 13])
  (sort ["z" "x" "a" "aa"])
  (sort (comp - compare) ["z" "x" "a" "aa"])

  ; FAIL
  (sort [:y "2" 33 :x])
  ; but with own comparator - works again
  (sort #(compare (str %1) (str %2)) [1 "-2" 33 :x])

  ;; this sorts by the first element.
  (sort [[:a 7], [:c 13], [:b 21]])

  ; but this naive attempt to sort by the second fails
  (sort second [[:a 7], [:c 13], [:b 21]])
  ;; but this works
  (sort-by second [[:a 7], [:c 13], [:b 21]])
  (sort-by second (comp - compare) [[:a 7], [:c 13], [:b 21]])


  (sort-by str [1 "-2" 33 :x])
  (sort-by str (comp - compare) [1 "-2" 33 :x])

  ;; 22 3 34 444 5
  (sort-by #(str (second %))
           [[1 3 4] [:x "5"] [33 "444"] [33 34 34] [33 22]])

  ;; 3 5 22 34 444
  (sort-by #(Long/parseLong (str (second %)))
           [[1 3 4] [:x "5"] [33 "444"] [33 34 34] [33 22]])

  ;; 444 34 22 5 3
  (sort-by #(Long/parseLong (str (second %)))
           (comp - compare)
           [[1 3 4] [:x "5"] [33 "444"] [33 34 34] [33 22]])

  ;; since keywords are functions, this works
  (sort-by :age [{:age 99 :name "grandpa"}, {:age 13 :name "son"}, {:age 2 :name "baby"} {:age 32 :name "mother"}])

  (def plays [{:band "Burial",     :plays 979,  :loved 9}
              {:band "Z",     :plays 12979,  :loved 1}
              {:band "A",     :plays 12979,  :loved 1}
              {:band "Eno",        :plays 2333, :loved 15}
              {:band "Slayer",     :plays 12979,  :loved 1}
              {:band "Bill Evans", :plays 979,  :loved 9}
              {:band "Magma",      :plays 2665, :loved 31}
              {:band "Compare",    :plays 979,  :loved 10}])
  (def sort-by-loved-ratio (partial sort-by #(/ (:plays %) (:loved %))))
  (sort-by-loved-ratio plays))

;; imagine we would like to: (sort-by (columns [:plays :loved :band]) plays)
(comment
  ; so columns should return a function that returns a vector of the three values of :plays :loved and :band
  ( #(vector (:plays %) (:loved %) (:band %))
    {:band "Burial",    :plays 979,  :loved 9})

  ( #(vec (map (fn [k] (k %)) [:plays :band]))
    {:band "Burial",    :plays 979,  :loved 9})

  (defn values-as-vec [keys-vec the-map]
    (vec (map (fn [k](k the-map)) keys-vec)))

  (values-as-vec [:plays :loved :band]
    {:band "Burial",    :plays 979,  :loved 9})

  ;; now bringing it all together
  (defn columns [keys-vec]
    (fn [row]
      (vec (map (fn [k](k row)) keys-vec))))

  ((columns [:plays :loved :band]) {:band "Burial",    :plays 979,  :loved 9})

  (sort-by (columns [:plays :loved :band]) plays)

  nil)

(def metal [{:band "AD/DC", :song "Whole Lotta Rosie" :rank 2, :country "Australia"}
            {:band "AD/DC", :song "Let There Be Rock" :rank 1,  :country "Australia"}
            {:band "AD/DC", :song "Witch's Spell" :rank 3, :country "Australia"}
            {:band "Metallica", :song "Lux Æterna" :rank 3, :country "USA"}
            {:band "Metallica", :song "For Whom The Bell Tolls" :rank 2, :country "USA"}
            {:band "Metallica", :song "Whiplash" :rank 1, :country "USA"}
            {:band "Slayer", :song "South Of Heaven":rank 2, :country "USA"}
            {:band "Slayer", :song "World Painted Blood":rank 1, :country "USA"}])

(sort-by :rank metal)
(->> metal ; sort-by does stable sorting. Therefore, this works:
     (sort-by :band)
     (sort-by :rank >))


;;----------------------------------------
;; closure
;;----------------------------------------

(defn divisible [denom]
  (fn [num]
    (zero? (rem num denom)))) ;; bind denom to the anonymous function

((divisible 7) 21)

(filter (divisible 7) (range 1 71))


;;----------------------------------------
;; many things may be called as a function
;;----------------------------------------


(instance? IFn #(str "an anonymous function"))
(defn a-named-fun [p1 p2] (str "two params " p1 " " p2))
(instance? IFn a-named-fun)
(instance? IFn :an-ordanary-keyword)
(instance? IFn {:a "map"})
(instance? IFn #{"a" "set" "too"})

