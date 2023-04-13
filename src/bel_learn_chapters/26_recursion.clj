(ns bel-learn-chapters.26-recursion)



;; stack overflow...
(defn pr-recursive-1 [n acc]
  (if (> n 0)
    (pr-recursive-1 (dec n) (conj acc n))
    acc))

(pr-recursive-1 5 [])


;;----------------------------
;; tail recursive
;;----------------------------

;; recur to function call
(defn pr-recursive-2 [n acc]
  (if (> n 0)
    (recur (dec n) (conj acc n))
    acc))

(pr-recursive-2 5 [])

;;-------------------------------------
;; tail recursive with loop entry point
;;-------------------------------------

;; recur to loop
(loop [i   0
       acc []]
  (if (> i 10)
    acc
    (recur (inc i) (conj acc (keyword (str "k-" i))))))

;;------------------------------------------
;; create accumulator without loop but arity
;;------------------------------------------

;; multi arity to start with empty accumulator
(defn pr-recursive-3
  ([n] (pr-recursive-3 n []))
  ([n acc] (if (> n 0)
             (recur (dec n) (conj acc n))
             acc)))

(pr-recursive-3 5)


;;------------------------------------------
;; create accumulator with loop
;;------------------------------------------

;; accumulator inside with loop
(defn pow [n m]
  (loop [acc 1
         m   m]
    (if (= 0 m)
      acc
      (recur (* n acc) (dec m)))))

(pow 2 10)
(pow 2 0)


;; trampoline
;; may be used when "not tail call is needed" or
;; with mutual recursion calls

;;------------------------------------------
;; simple trampoline
;;------------------------------------------

(defn pow-t [n m acc]
  (if (= 0 m)
    acc
    (fn [] (pow-t n
                  (dec m)
                  (* n acc)))))

(trampoline (pow-t 2 10 1))

;;------------------------------------------
;; Trampoline with mutual calls.
;; letfn works kind of foward declaration.
;;------------------------------------------
(defn jump []
  (letfn [(rand-jump [] (rand-nth (flatten [(repeat 2 salto)
                                            (repeat 3 plain)
                                            (repeat 1 backf)
                                            (repeat 1 end)])))
          (salto [] (do (println "salto") (rand-jump)))
          (plain [] (do (println "plain") (rand-jump)))
          (backf [] (do (println "backflip") (rand-jump)))
          (end [] (do (println "end")))]
    (rand-jump)))


(trampoline (jump))