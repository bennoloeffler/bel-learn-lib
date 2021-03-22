(ns bel-learn-chapters.20-lazy)


;;-------------------------------------
;; only do the effect, when realizing...
;;-------------------------------------

(defn effect []
 (println "effect")
 6)

(def lazy (lazy-seq [ 1 2 3 (effect)]))

; now it will be realized
lazy


;;-------------------------------------
;; endless loop... of zeros
;;-------------------------------------

(defn zeros []
  (lazy-seq (cons 0 (zeros))))

(take 5 (zeros))

; dont't do this! Endless loop
;(zeros)

;;-------------------------------------
;; only do the effect, when realizing...
;;-------------------------------------


(defn even-nums
 ([n] (lazy-seq (cons n (even-nums (+ n 2)))))
 ([] (even-nums 0)))

; since lazy, it can be really huge... 500 Mio
(def many-evens (take 500000000 (even-nums)))
(take 5 many-evens)


;;-------------------------------------
;; some more example
;;-------------------------------------

(defn multiplen
  ([]
   (multiplen 1 1))
  ([total x]
   (let [new-total (*' total x)]
     (lazy-seq
       (cons new-total (multiplen new-total (inc x)))))))

(def x ( multiplen))
(type x)
(take 50 x)
(take 50 (vector 1 2 3 4 (inc 8)))

nil
