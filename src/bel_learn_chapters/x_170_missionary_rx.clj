(ns bel-learn-chapters.x-170-missionary-rx
  (:require [missionary.core :as m]))

; this is a reactive computation, the println reacts to input changes
(comment
  (def !input (atom 1))
  (def main (m/reactor
              (let [>x (m/signal! (m/watch !input))       ; continuous signal reflecting atom state
                    >y (m/signal! (m/latest + >x >x))]    ; derived computation, diamond shape
                (m/stream! (m/ap (println  "bel: " (m/?< >y))))))) ; discrete effect performed on successive values

  (def dispose! (main #(prn ::success %) #(prn ::crash %)))
  ; 2
  (swap! !input inc)
  ; 4
  (dispose!))