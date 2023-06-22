(ns bel-learn-chapters.95-macros
  (:require [puget.printer :refer (cprint)])
  (:use tupelo.core))


;; https://stackoverflow.com/questions/53014679/recursive-macro-clojure
;; https://www.braveclojure.com/writing-macros/
;; https://learnxinyminutes.com/docs/clojure-macros/
;; https://purelyfunctional.tv/mini-guide/when-to-use-a-macro-in-clojure/
;; http://clojure-doc.org/articles/language/macros.html
;; https://abhinavomprakash.com/posts/macrobrew/
;; joy of clojure, chapter 8

;; macro in cljs AND clj
;; https://github.com/cgrand/macrovich
;; https://clojureverse.org/t/how-to-define-and-use-a-macro-in-both-clj-and-cljs/2896
;; https://clojureverse.org/t/need-help-with-writing-macros-for-clojurescript/6671/6
;;
;; Keybinding: CTRL-SHIFT-# expand macro
;;


(defn try-spy
  [arg]
  (spyx (+ arg 4)))

(comment (try-spy 6))

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

;;
;; ---------- recursion ----------
;;


(defmacro rev-list
  ;"reverses the sequence of elements in (recursive) lists"
  [& list]
  (assert (seqable? list))
  (let [r-list# (reverse list)]
    ;>> (spy r-list#)]
    (map (fn [e#]
           (if (and (seqable? e#) (> (count e#) 1))
             `(rev-list ~@e#)
             e#))
         r-list#)))

(comment
  (def m '(rev-list (1 9 +) 16 (2 (2 1 +) +) 4 +))
  (macroexpand-1 m)
  (macroexpand m)
  (clojure.tools.analyzer.jvm/macroexpand-all m)
  (rev-list (1 9 +) 16 (2 3 +) 4 +)
  (clojure.core/reverse '(5 4 +)))

