(ns bel-learn-chapters.99-all-the-rest
  (:gen-class))

(require '[clojure.repl :refer :all])
(require '[clojure.pprint :refer :all])



(set! *print-length* 3)

(comment


  ;;
  ;; TYPES, ARGS
  ;;

  (def cats 5)
  (type cats) ;Long
  (type 'cats) ;Symbol
  (type #'cats) ;VAr
  (str bel-learn-chapters.99-all-the-rest/cats) ; to string with full name

  'inc ; inc ; the symbol
  (resolve 'inc) ; #'clojure.core/inc ; the var
  (eval 'inc) ; #<core$inc clojure.core$inc@16bc0b3c> ; the value

  (def third (fn [number] (/ number 3)))

  (third 13) ; 13/3

  (type (third 13)) ; Ratio

  (defn half
    ([]  1/2)
    ([x] (/ x 2)))

  (half 5)
  (half)



  ;;
  ;; SEQUENCE
  ;;

  (def s-to-inc [1 2 3 4])
  ;(defn inc-seq [the-seq]
  ;  (when (seq the-seq)
  ;    [(inc (first the-seq)) (inc-seq (rest the-seq))]))
  ;(def inc-seq)
  ;(println (inc-seq s-to-inc))
  (println s-to-inc)

  (defn inc-seq [the-seq]
    (if (first the-seq)
      (cons (inc (first the-seq)) (inc-seq (rest the-seq)))
      (list)))

  (defn inc-seq-2 [the-seq]
    (if (seq the-seq)
      (cons (inc (first the-seq)) (inc-seq-2 (rest the-seq)))
      (list)))

  (println (inc-seq-2 s-to-inc))

  (println (inc-seq (range 1 100)))

  (defn transform-all [f all]
    (if (first all)
      (cons (f (first all)) (transform-all f (rest all)))
      (list)))

  (defn transform-all-2 [f all]
    (loop [in all
           out []]
      (if-not (seq in)
        out
        (recur (rest in)
               (conj out (f (first in)))))))


  (transform-all keyword ["abc" "xyz"])
  (transform-all-2 keyword ["abc" "xyz"])
  (transform-all-2 keyword  (map str (range 10000)))

  (map keyword ['b 'c]) ; map by function

  {:year  1969
   :event "moon landing"} ; also a mapping... by type


  ;;
  ;; FUNCTION
  ;;

  (doc pos?)

  (defn expand
    "similar to iterate"
    [f x count]
    (when (pos? count)
      (cons x (expand f (f x) (dec count)))))

  (defn expand-2
    "apply f starting with start count times"
    [f start count]
    (loop [next start
           count count
           result []]
      (if-not (pos? count)
        result
        (recur (f next) (dec count) (conj  result next)))))

  (doc expand)

  (expand inc 0 10)
  (expand-2 half 100 100)

  ;; => (0 1 2 3 4 5 6 7 8 9)

  (take 10 (iterate inc 0))
  (take 30 (iterate (fn [x] (if (odd? x) (+ 1 x) (/ x 2))) 17869))

  (take 10 (repeat "so"))
  (repeat  3 "aha")
  (take 10 (repeatedly rand))
  (range 2 10) ; (2 3 4 5 6 7 8 9)
  (range 0 100 5); (0 5 10... 95)
  (take 10 (cycle [1 2 3]))

  (map (fn [n vehicle] (str "I've got " n " " vehicle "s"))
       [0 200 9]
       ["car" "train" "kiteboard"])


  (map (fn [index element] (str index ". " element))
       (iterate inc 0)
       ["erlang" "ruby" "haskell"])

  (map-indexed (fn [index element] (str index "-" element))
               ["Sabine" "Benno" "Paul" "Leo"])

  (concat [1 2 3] [:a :b :c] [4 5 6])

  (interleave [:a :b :c] (range 1 100))


  (use '[clojure.string :only (join split)])
  (join (interpose " <-> "  (map str (take 10 (iterate inc 0)))))

  (interpose :and [1 2 3 4])

  (map + [1 2 3]
       [4 5 6]
       [7 8 9])

  (def concat-list (concat [1 2 3] [:a :b :c] [4 5 6] '(7 8) #{9 10} {:a 11 :b 12}))

  (type (nth  (vec concat-list) 13))

  (apply str (reverse "woolf"))

  (max [1 2 3]) ;;=> [1 2 3]
  (apply max [1 2 3]) ;;=> 3
  ;; which is the same as
  (max 1 2 3) ;;=> 3

  (seq "sato") ; (\s \a \t \o)

  (apply str (shuffle (seq "abracadabra")))

  (drop 3 (range 10))

  (split-with number? [1 2 3 :mark 4 5 6 :mark 7])

  (filter pos? [1 5 -4 -7 3 0])

  (remove string? [1 "turing" :apple])

  (partition 2 [:cats 5 :bats 27 :crocodiles 0])

  (partition-by neg? [1 2 3 2 1 -1 -2 -3 -2 -1 1 2])

  (frequencies [:meow :mrrrow :meow :meow])

  ;(use '[clojure.pprint :only '(pprint)])
  (clojure.pprint/pprint (group-by :first [{:first "Li"    :last "Zhou"}
                                           {:first "Sarah" :last "Lee"}
                                           {:first "Sarah" :last "Dunn"}
                                           {:first "Li"    :last "O'Toole"}]))

  (take 10 (iterate inc 0))
  ;; => (0 1 2 3 4 5 6 7 8 9)

  (take 10 (filter odd? (iterate inc 0)))
  ;; (1 3 5 7 9 11 13 15 17 19)

  (take 10 (partition 2 (filter odd? (iterate inc 0))))

  (take 10 (partition 2 1 (filter odd? (iterate inc 0))))

  (reduce + (take 1000 (map #(* (first %) (second %)) (partition 2 1 (filter odd? (iterate inc 0))))))

  (reduce +
          (take 1000
                (map #(* (first %) (second %))
                     (partition 2 1
                                (filter odd?
                                        (iterate inc 0))))))

  (->> 0
       (iterate inc)
       (filter odd?)
       (partition 2 1)
       (map (fn [pair]
              (* (first pair) (second pair))))
       (take 1000)
       (reduce +))

  ;;
  ;; FUNCTION ARITY, VARIADIC
  ;;

  (defn greet
    ([to-whom message]
     (println (str "Hello " to-whom ", " message)))
    ([message]
     (greet "stranger" message)))

  (greet "Benno" "nice to meet you")
  (greet "did we ever meet?")


  (defn vargs
    [x y & more-args]
    {:x    x
     :y    y
     :more more-args})

  (vargs 1 2)
  (vargs 1 2 3 4 5 6 7 8)


   ;;
   ;; MULTI-FUNCTION
   ;;

  (defn dispatch-number-format [num-s]
    (cond
      (string? num-s) :str-s
      (seqable? num-s) :seq-s
      (number? num-s) :num-s))

  (defmulti one-number-added dispatch-number-format)

  (defmethod one-number-added :seq-s [num-s]
    (println "type -> " (type num-s))
    (reduce + num-s))

  (defmethod one-number-added :num-s [num-s]
    (println "type -> " (type num-s))
    num-s)

  (defmethod one-number-added :str-s [num-s]
    (println "type -> " (type num-s))
    (println num-s)
    (Integer/parseInt num-s))

  (defmethod one-number-added :default [num-s]
    (println "type -> " (type num-s))
    0)

  (one-number-added 3)
  (one-number-added [3 4 5 6])
  (one-number-added "-32")
  (one-number-added :undefined)

  ;;
  ;; PARTIAL
  ;;

  (def my-inc (partial + 1))
  (my-inc 15)

  ;;
  ;; PRE POST CONDITIONS
  ;;

  (defn double-big [x]
    {:pre [(> x 10)] :post [(< % 30)]}
    (* 2 x))
  ;(double-big 9)                                              ; fails
  (double-big 11)                                             ;ok
  ;(double-big 15)                                             ; fails

  (defn publish-bel [a-book]
    {:pre [(:title a-book)] :post [(:published %)]}
    "doc-string"
    (println (:title a-book))
    (assoc a-book :published "1.1.1970"))


  (def a-book {:title "the goal"})
  (publish-bel a-book)

  (def b-book {:the-title "the goal"})
  ;(publish-bel b-book) ;FAILS


  ;;
  ;; MACRO
  ;;

  (defmacro rev [fun & args]
    (cons fun (reverse args)))
  (macroexpand '(rev str "hi" (+ 1 2)))
  (eval (macroexpand '(rev str "hi" (+ 1 2))))
  (rev str "hi" (+ 1 2)); 3hi statt hi3

  (let [x 2] `(inc x))
  (let [x 2] `(inc ~x))
  (eval (let [x 2] `(inc ~x)))
  `(foo ~[1 2 3])
  `(foo ~@[1 2 3])
  (source or)

  (gensym "hi"); uniquie symbol
  `(let [x# 2] x#); auto symbol

  ;;
  ;;   CONTROL FLOW
  ;;

  (do
    (println [1 2 3])
    (println {:a 4 :b 17}))

  (if (> 2 5)
    (str "2 bigger 5")
    (str "5 bigger 2"))

  (when true
    (prn :hi)
    (prn :there))

  (if-not (vector? (list 1 2 3))
    :a
    :b)

  (when-let [x (first [])]
    (str x))

  ;; side effect!
  (def x 0)
  (while (< x 5)
    (prn x)
    (def x (inc x)))

  (cond
    (pos? -12) :yes-we-can
    (= (+ 1 2) 13) "sowas"
    (not= true (pos? 3)) (apply str [1 2 3])
    :else 17)

  (defn category
    "Determines the Saffir-Simpson category of a hurricane, by wind speed in meters/sec"
    [wind-speed]
    (condp <= wind-speed
      70 :F5
      58 :F4
      49 :F3
      42 :F2
      :F1)) ; Default value

  (category 48)


  (defn with-tax
    "Computes the total cost, with tax, of a purchase in the given state."
    [state subtotal]
    (case state
      :WA (* 1.065 subtotal)
      :OR subtotal
      :CA (* 1.075 subtotal)
      ; ... 48 other states ...
      subtotal)) ; a default case





  ;;
  ;; RECURSION
  ;;

  (defn sum-numbers
    ([list-of-numbers]
     (sum-numbers 0 list-of-numbers))
    ([val list-of-numbers]
     (if-let [n (first list-of-numbers)]
       (recur (+ n val) (rest list-of-numbers))
       val)))

  (sum-numbers '(1 2 3))
  ;(sum-numbers (range 100000000)) ; no stack - but 100 Mio times function call...

  (loop [i 0
         expanded []]
    (if (> i 10)
      expanded
      (recur (inc i) (conj expanded i))))

  (defn sum-something [values]
    (loop [vs values total 0]
      (if (empty? vs)
        total
        (recur
         (rest vs)
         (+ total (first vs))))))

  (sum-something (range 1 1000000))

  ;;
  ;; LAZINESS
  ;;

  (defn integers ; ENDLESS!
    [x]
    (lazy-seq
     (cons x (integers (inc x)))))

  ;(println (integers 0))

  (take 10 (integers 3))

  (def dl (delay (concat [0 0 0 0 0 0 0 0] '(1 2 3 4 5 6 7 8 9))))
  (deref dl)
  (def ll (concat [0 0 0 0 0 0 0 0] '(1 2 3 4 5 6 7 8 9)))
  (take 12 ll) ; lazy-seq

  (def x-delayed (delay
                  (println "computing a really big number!")
                  (last (take 10000 (iterate inc 0)))))

  (deref x-delayed)

  ;;
  ;; LIST COMPREHENSION

  (for [x (range 45 77)]
    (- x))

  (for [x [1 2 3]
        y [:a :b]]
    [x y])


  (take 5
        (for [x     (range 5)
              y     (range 5)
              :when (and (even? x) (odd? y))]; alternative is :while
          [x y]))

  ;;
  ;; -> ->>
  ;;

  (require '[clojure.walk])
  (pprint (clojure.walk/macroexpand-all
           '(->>
             (range 10)
             (filter odd?)
             (reduce +))))
  (reduce + (filter odd? (range 10)))

  (->> (range 10)
       (filter odd?)
       (reduce +))

  (-> {:proton :fermion}
      (assoc :photon :boson)
      (assoc :neutrino :fermion))

  ;;
  ;; IMMUTABILITY
  ;;

  (defn present
    [gift]
    (fn [] gift)) ; closure - return bound function and remember that

  (def red-gift (present "vuvuzeela"))
  (def green-gift (present "picture office"))
  (green-gift)
  (red-gift)

  ;;
  ;; DELAY and FUTURES and PROMISES
  ;;

  (def later (delay (prn "Adding") (+ 1 2)))
  (deref later)
  @later
  later

  ; start another thread - and then wait until its finished at deref
  (def xf (future (prn "hi") (+ 1 2)))
  (deref xf)
  (deref xf); only evaluated once. "hi" is printed only once. fn value CACHED!

  ; multithreaded!
  (dotimes [i 50] (future (prn i)))

  (def box (promise))
  ;box
  ;(deref box)
  (deliver box "Benno")
  (deliver box "Sabine")

  (def card (promise))
  (def dealer (future
                (Thread/sleep 500)
                (deliver card [(inc (rand-int 13))
                               (rand-nth [:clubs :spades :hearts :diamonds])])))
  (deref card)

  (def ^:dynamic *board* :maple)
  (defn cut [] (prn "sawing through" *board*))
  (cut)
  (def ^:dynamic *board* :apple)
  (binding [*board* :cedar] (cut))

  ;;
  ;; ATOMS AND REFS
  ;;

  (def xs (atom #{}))
  (println xs)
  (println @xs)
  (println (deref xs))

  (reset! xs 5)
  (swap! xs inc)
  (swap! xs inc)

  (def a (atom #{}))
  (dotimes [i 100] (future (swap! a conj i)))

  ;; REFS - transactions

  (def x (ref 0))
  (def y (ref 0))
  (dosync
   (ref-set x 1)
   (ref-set y 2))

  [@x @y]

  ; complete transation: alter
  (dosync
   (alter x + 2)
   (alter y inc))

  [@x @y]

  ; No complete transaction: commute
  (dosync
   (commute x + 2)
   (commute y inc))

  (dosync
   (alter x + (ensure y))))
