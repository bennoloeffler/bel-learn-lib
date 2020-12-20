(ns bel-learn-chapters.20-lazy)

(defn zeros []
  (lazy-seq (cons 0 (zeros))))

(take 5 (zeros))

(cons 2 [3 4])

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
