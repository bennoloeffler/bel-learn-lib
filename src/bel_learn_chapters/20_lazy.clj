(ns bel-learn-chapters.20-lazy)


;;-------------------------------------
;; only do the effect, when realizing...
;;-------------------------------------
(comment

  (defn effect []
    (println "effect")
    "tada!")

  (def lazy (concat
              (range 2)
              (lazy-seq [(effect)])))

  ; effect not yet realized
  (take 1 lazy)

  ; now it will be realized
  (take 3 lazy))


;;-------------------------------------
;; endless loop... of zeros or integers
;;-------------------------------------

(comment

  (defn zeros []
    (lazy-seq (cons 0 (zeros))))

  (take 5 (zeros))

  ; dont't do this! Endless loop
  ;(zeros)

  (defn ints
    ([x] (lazy-seq (cons x (ints (inc x)))))
    ([] (ints 0)))

  (take 5 (ints)))

;;-------------------------------------
;; only do the effect, when realizing...
;;-------------------------------------

(comment
  (defn even-nums
    ([n] (lazy-seq (cons n (even-nums (+ n 2)))))
    ([] (even-nums 0)))

  ; even take is lazy... since lazy, it can be really huge...
  (def many-evens (take 50000000000000000000000000000000N (even-nums)))
  (take 5 many-evens)
  ; same
  (take 5 (even-nums)))


;;-------------------------------------
;; some more example
;;-------------------------------------

; assume, we want this:
; (steps [1 2 3 4])
; => [1 [2 [3 [4 []]]]]

(comment
  (defn steps [v]
    (if (seq v)
      [(first v) (steps (rest v))]
      []))

  (steps [1 2 3 4])
  (steps (range 10))

  ; does not work
  (steps (range 100000))

  (defn steps-lazy [v]
    (lazy-seq (if (seq v)
                [(first v) (steps-lazy (rest v))]
                [])))

  (dorun (steps-lazy (range 100000))))

;;----------------------------------
;; from the joy of clojure, page 126
;;----------------------------------

; 1    Use the lazy-seq macro at the outermost level of your lazy sequence–producing expression(s).
; 2    If you happen to be consuming another sequence during your operations, then use rest instead of next.
; 3    Prefer higher-order functions when processing sequences.
; 4    Don’t hold on to your head.

(comment
  (let [r (range 1e9)]
    (first r)
    (last r))
  ;=> 999999999

  (let [r (range 1e9)]
    (last r)
    (first r))
  ;java.lang.OutOfMemoryError: GC overhead limit exceeded
  nil)

;;------------------------------
;; create endless lazy sequences
;;------------------------------

(comment
  (def half-of-half (iterate (fn [n] (/ n 2)) 1))
  (take 5 half-of-half)

  (def benno-again-and-again (cycle [:be "nn" 0]))
  (take 20 benno-again-and-again)

  (def once-rand-and-then-no-rand (repeat (rand-int 10)))
  (take 4 once-rand-and-then-no-rand)
  (take 2 once-rand-and-then-no-rand)

  (def lazy-seq-of-function-calls (repeatedly #(rand-int 10)))
  (take 5 lazy-seq-of-function-calls))


;;----------------
;; another example
;;----------------

(comment
  (defn multiplen
    ([]
     (multiplen 1 1))
    ([total x]
     (let [new-total (*' total x)]
       (lazy-seq
         (cons new-total (multiplen new-total (inc x)))))))

  (def x (multiplen))
  (type x)
  (take 6 x))


;;----------------
;; assume expensive function and
;; see what map does in terms of lazy...
;;----------------

(comment

  (defn square-heavy
    "A very expensive calculation."
    [n]
    (println "squaring" n)
    (Thread/sleep 100)
    [n :--> (* n n)])

  (time (square-heavy 3))

  ;; This does apply square-heavy.
  ;; Lazy in 32er chunks... So 3 are realized.
  (defn all-squares [up-to]
    (map square-heavy (range up-to)))

  (time (println (take 1 (all-squares 3))))

  ;; Now, with size of 10000, lazy makes more sense...
  ;; 32 are realized - not 10.000
  (time (println (take 1 (all-squares 10000))))

  (def buffered-values (take 5 (all-squares 10000)))
  ;; first time, calc
  (time (println buffered-values))
  ;; second time, do not calc any more
  (time (println buffered-values))

  ;; although the first chunk almost is enough, the second chunk
  ;; is realized completely
  (take 2 (drop-while #(< (% 2) 1000) (all-squares 10000))))


;;----------------------------------
;; defer as another way of lazyness
;;----------------------------------

(defn defer-expensive [cheap expensive]
  (if-let [good-enough (force cheap)]
    good-enough
    (force expensive)))

(defer-expensive (delay :cheap)
                 (delay (do (Thread/sleep 5000) :expensive)))
;=> :cheap
(defer-expensive (delay false)
                 (delay (do (Thread/sleep 5000) :expensive)))
;=> :expensive


;;----------------------------------
;; quicksort lazy
;;----------------------------------
(def comp-count (atom 0))

(defn q-sort [coll]
  (lazy-seq
    (loop [[work & remaining] coll]
      ;(println "work: " work)
      ;(println "remaining: " remaining)

      (if-let [[pivot & others] (seq work)]
        (do ;(println "pivot: " pivot)
            ;(println "others: " others)
            (let [smaller?    #(do (swap! comp-count inc)
                                   (< % pivot))
                  smaller     (filter smaller? (seq others))
                  not-smaller (remove smaller? (seq others))]
              (recur (list* smaller pivot not-smaller remaining))))
        (when-let [[work & remaining] remaining]
          (cons work (q-sort remaining)))))))

(defn qsort [xs]
  (reset! comp-count 0)
  (q-sort (list xs)))

(comment

    (time (do (dorun (take 1 (qsort (shuffle (range 1000)))))
              @comp-count))

    ;; if you take more elements, there are MUCH MORE comparisons
    ;; about x 10 to x 20
    (time (do (dorun (take 1000 (qsort (shuffle (range 1000)))))
              @comp-count)))




