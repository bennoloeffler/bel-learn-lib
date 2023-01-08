(ns bel-learn-chapters.x-140-reducers
  [:require [clojure.core.reducers :as r]
            [clojure.java.io :as io]])

;; READ THIS: https://hub.packtpub.com/parallelization-using-reducers/
;; https://clojure.org/reference/reducers
;; https://www.braveclojure.com/quests/reducers/know-your-reducers/
;; see dropbox, clojure books:
;; 120 reducers - Parallel Programming in Clojure with Reducers.pdf
;; https://eli.thegreenplace.net/2017/reducers-transducers-and-coreasync-in-clojure/

(comment

  ; this is the bread and butter of
  ; functional programming
  (reduce + 100 [1 2 3])

  ; a reducing-function (+ in the example) is
  ; applied to a starting value (100) and the first
  ; value in a collection (1). Result is 101.
  ; This will repeat
  ; (+ 101 2) = 103
  ; (+ 103 3) = 106
  ; until the collection is exhauseted.

  ; a reducing-function in general has the signature
  ; (fn [accumulator next-value-of-coll]...
  (defn add-subvec-to-vec [acc val]
    ;(println "acc:" acc "val: " val)
    (clojure.string/join [acc  (clojure.string/join (repeat val val))]))
  ; this one joins to a string - val times the digit of the val
  (reduce add-subvec-to-vec "" [2 3 1]) ; => "223331"

  ; reduce is implemented like that
  (defn my-reduce
    ([reducing-f initial-val coll]
     (loop [c   coll
            acc initial-val]
       (if (seq c)
         (recur (rest c) (reducing-f acc (first c)))
         acc)))
    ([reducing-f coll] ;   (reducing-f) WHY? see below
     (my-reduce reducing-f (reducing-f) coll)))

  (reduce + 100 [1 2 3])
  (reduce + [1 2 3])
  (my-reduce + 100 [1 2 3])
  (my-reduce + [1 2 3])
  ;; THIS is the case, the call to
  ;; (reducing-f) it's really needed:
  (my-reduce + []) ; no init value and no elem in coll
  (my-reduce + 100 []) ; this return init-value
  (reduce + []) ; + has a 0-arity (+)

  ;; this is a typical use case
  (def s (range (* 10 1000 1000)))
  (time (->> s
             (filter even?)
             (map inc)
             (reduce +)))

  ;; could that be
  ;; 1. one combined function like with (comp ... ...)
  ;; 2. that does not create intermediate sequences
  ;; 3. processed in parallel
  (def sv (vec (range (* 10 1000 1000))))
  (time (->> sv
             (r/filter even?)
             (r/map inc)
             (r/fold +)))
  ;; how does the [clojure.core.reducers :as r]
  ;; work? lets have a look what r/map returns:
  (def r (r/map inc [1 2 3]))
  (ancestors (type r))
  ;; CollFold and CollReduce ???
  (repl)
  (show-members r)
  ;; coll-reduce and coll-fold ???

  (reduce + r) ; reduce works
  (r/fold + r) ; fold, too
  (first r) ; BUT THIS FAILS. WHY?
  ;; because a CollReduce or CollFold is no Collection and
  ;; cannot be turned into a Sequence.
  ;; But it can be reduced! Even parallely with r/fold.
  ;; It's a reducible, foldable something...
  ;; It's not a coll, but a recipie how to produce a coll.

  ;; to make it a collection again, do
  (into [] r)
  (r/foldcat r)

  ;; fold may have a merge function, which combines
  ;; the junks that are separately, parallely processed.

  ; start with the reduding-function
  (defn count-words
    ([] {})
    ([freqs word]
     ;(println "word:" word ", freqs:" freqs)
     (assoc freqs word (inc (get freqs word 0)))))

  ;; THERE IS A TINY, but useful difference
  ;; between r/reduce and reduce
  (reduce count-words {} ["a" "b" "a"])
  (reduce count-words ["a" "b" "a"]) ;; starts with acc=a and val=b
  (r/reduce count-words ["a" "b" "a"]) ;; calls (f) for initial acc

  ;; this is the function, that merges the
  ;; results from the parellel reduces
  (defn merge-counts
    ([] {})
    ([& m] (apply merge-with + m)))

  ; reduce way to do it
  (defn word-frequency-reduce [words]
    (reduce count-words {} words)) ;; INIT {} needed!
  ; (r/reduce count-words words) would do it

  ; fold way to do it
  (defn word-frequency-fold [words]
    (let [n (/ (count words) 3)] ; use 8 parallel work chunks
      ;; the default of 512 elements is much too small...
      (r/fold n merge-counts count-words words)))

  #_(defn word-frequency-fold [words]
      (r/fold merge-counts count-words words))

  ; get some massive data...
  (def huge-str (slurp (io/resource "moby-dick.txt")))
  (set! *print-length* 100)
  (def words (as-> huge-str $
                 (clojure.string/split $ #"[—|-|;|\?|\.|,|)|(|\s+]")
                 (remove #(= "" %) $)
                 (vec $)))

  ;; No real performance differences...
  (time
    (def word-frequencies
      (word-frequency-fold words)))

  (time
    (def word-frequencies-slower
      (word-frequency-reduce words)))

  (repl)
  (ea/assert (= word-frequencies-slower word-frequencies))

  (take 1000 (sort-by #(- (val %)) word-frequencies))

  (reduce (fn [acc entry] (+ acc (val entry)))
          0 word-frequencies)

  nil)


(comment

  ;; performance differences are visible,
  ;; when the transformation pipeline becomes longer

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

  (defn transform-with-reducer [col]
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

  (time (transform-with-reducer l)) ; 9 secs list! (no intermediate
  (time (transform-with-reducer v)) ; 4 (no intermediate col, parallel)

  ;; wouldn't it be great, if the transformation-pipeline
  ;; could be reused?
  ;; This is, what transducers do:
  ;; They decouple the transformation from the source.
  nil)




