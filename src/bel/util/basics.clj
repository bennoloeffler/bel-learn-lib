(ns bel.util.basics
  (:require
    [puget.printer :refer (cprint)]
    [erdos.assert :as pa]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; BELs utils
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(use 'hashp.core)
;(use 'debux.core)

(defmacro dbg-bel [body]
  `(let [body# ~body]
     (cprint :---expresion-->)
     (cprint '~body)
     (cprint :---result-is-->)
     (cprint body#)
     (cprint :---fin--------)
     body#))


(defn != [x y]
  (not= x y))

(comment
  (pa/assert (dbg-bel (!= (* 2 3) (- 10 5)))))
