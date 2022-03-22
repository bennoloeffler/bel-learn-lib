(ns bel-learn-chapters.x-150-transducers
  (:require [clojure.core.async :as a]))

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

