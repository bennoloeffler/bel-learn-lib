(ns bel-learn-chapters.x-150-transducers
  (:require [clojure.core.async :as a]))

; https://eli.thegreenplace.net/2017/reducers-transducers-and-coreasync-in-clojure/

(comment

  ; combined function - applied only once. No intermediate sequence.
  (def xform
    ; create transducer (ommiting the col)
    (comp (map inc)
          (filter even?)))

  ; apply transducer
  (->> (range 10)
       (sequence xform)
       (prn "result is "))


  ; now with core async
  (do
    (def result (a/chan 10))

    ; a channel uses transducer to transform its input
    (def transformed (->> (a/pipe result (a/chan 10 xform))
                          (a/into [])))

    (a/go (prn "result is " (a/<! transformed)))
    (a/go (doseq [n (range 10)]
            (a/>! result n))
          (a/close! result))))


; https://eli.thegreenplace.net/2017/reducers-transducers-and-coreasync-in-clojure/
(comment

  (def s (range 0 10))
  (reduce + (map inc (filter even? s)))

  ;
  ; reducingf :: acc -> item -> acc
  ;

  ; map inc by reduce
  (reduce (fn [acc item] (conj acc (inc item))) [] [1 2 3 4 5])

  ; filter even? by reduce
  (reduce (fn [acc item] (if (even? item)
                           (conj acc item)
                           acc))
          [] [1 2 3 4 5])

  ;
  ; transformingf :: (acc -> item -> acc) -> (acc -> item -> acc)
  ;
  (defn mapping-transform
    [mapf]
    (fn [reducingf]
      (fn [acc item]
        (reducingf acc (mapf item)))))

  (reduce ((mapping-transform #(* % %)) +) 0 [1 2 3 4 5 6])

  (defn filtering-transform
    [predicate]
    (fn [reducingf]
      (fn [acc item]
        (if (predicate item)
          (reducingf acc item)
          acc))))

  (reduce ((filtering-transform even?) +) 0 [1 2 3 4 5 6])

  (reduce ((filtering-transform even?)
           ((mapping-transform inc) +)) 0 (range 0 10))

  ; in clojure, with transducers
  (reduce ((comp (filter even?) (map inc)) +) 0 sv))