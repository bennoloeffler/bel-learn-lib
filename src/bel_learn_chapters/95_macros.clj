(ns bel-learn-chapters.95-macros
  (:require [puget.printer :refer (cprint)]))


;;
;; ---------- quote and eval ----------
;;

'(+ 1 2 3)
(eval '(+ 1 2 3))

;;
;; ---------- args are not evaluated ----------
;;

(defmacro no-eval-of-args
  "docstring"
  [the-arg]
  (println "this is it: " the-arg))

(comment
  (no-eval-of-args (+ 1 3))
  (macroexpand '(no-eval-of-args (+ 1 3))))

;;
;; ---------- quote in macro ----------
;;

(defmacro only-quote
  []
  '(println "abc")) ;prevents from beeing executed immediately

(comment
  (only-quote)
  (macroexpand '(only-quote)))

;;
;; ---------- syntax-quote and unquote in macro ----------
;;

(defmacro quote-in-macro
  "docstring"
  [num]
  `(println (+ 50 ~num))) ;; otherwise its symbol num

(comment
  (quote-in-macro 5)
  (macroexpand '(quote-in-macro 5)))

;;
;; ---------- auto variables ----------
;;

(defmacro dbg-bel [body]
  `(let [body# ~body]
     (println "-------- dbg-bel --------------")
     (cprint ['~body :--> body#])
     (println)
     body#))

(comment
  (->> (range 10)
       dbg-bel
       (map inc)
       dbg-bel
       (map #(* % %))
       dbg-bel
       (reductions *)))

;;
;; ---------- splicing ----------
;;

(defmacro splice-macro
  [a-list]
  `(+ ~@a-list))

(comment
  (splice-macro (1 2 3 4))
  (macroexpand '(splice-macro (1 2 3 4))))