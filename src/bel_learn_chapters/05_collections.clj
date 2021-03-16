(ns bel-learn-chapters.05-collections)

(def v ["e1" "e2" "e3"]) ; vector of strings
(def l '(:a :b :c)) ; list of keywords
(def m {:name "Benno" :age 51 :born 1969}) ; map of two key-value-pairs
(def s #{2 1 4 3}) ; set has no double entries...

(v 1); get the second element of the vector v
(first l); get the first element of list l
(m :age) ; get the age in the map m
(s 0) ; test if set s contains element 0 -> nil means false

(first l)
(rest l)

(first v)
(rest v)

(first m)
(rest m)

(first s)
(rest s)

(next [1]) ; => nil
(rest [1]); => ()

;; first and rest are defined in terms of position, and work on anything that can be treated as an ordered collection
;; peek and pop work in terms of "natural insertion order" and only work with things that behave like a stack - (so not lazy-seqs, strings, etc.)
;; lists push and pop from the front, vectors push and pop from the end, queues push to one end, pop from the other
;; https://admay.github.io/queues-in-clojure/
;;
;; https://clojuredocs.org/clojure.core/conj
;;conj works on collections
(conj '(:b :c) :a) ; add before
(conj [:b :c] :a) ; add after

(cons :a '(:b :c)) ; add
(cons :a [:b :c]) ;returns collection

(defn half [n](/ n 2))

(defn expand
  "apply f starting with start count times"
  [f start count]
  (loop [next start
         count count
         result []]
    (if-not (pos? count)
      result
      (recur (f next) (dec count) (conj  result next))))) ; tail recursion


(expand half 10 5)

(type (iterate half 10)) ; an INFINITE sequence...? Lazy...
(take 5 (iterate half 5))
(take 10 (repeat "so"))
(take 10 (repeatedly #(rand-int 100)))

(map inc [1 2 3 4])
;(sort (vals (apply hash-map '(1 2 2 3 3 4 4 5))))
(map list [1 2 3 4])

(def choc {:title "Chocolate chip cookies"
           :ingredients {"flour"           [(+ 2 1/4) :cup]
                         "baking soda"     [1   :teaspoon]
                         "salt"            [1   :teaspoon]
                         "butter"          [1   :cup]
                         "sugar"           [3/4 :cup]
                         "brown sugar"     [3/4 :cup]
                         "vanilla"         [1   :teaspoon]
                         "eggs"            2
                         "chocolate chips" [12  :ounce]}})
(comment
  ((choc :ingredients) "butter"))
