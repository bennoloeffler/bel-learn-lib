(ns koans.17-macros
  (:require [koan-engine.core :refer [meditations]]))

(defmacro hello [x]
  (str "Hello, " x))

(defmacro infix [form]
  (list (second form) (first form) (nth form 2)))

(defmacro infix-better [form]
  `(~(second form) ; Note the syntax-quote (`) and unquote (~) characters!
    ~(first form)
    ~(nth form 2)))

(defmacro r-infix [form]
  (cond (not (seq? form))
        form ;...
        (= 1 (count form))
        `(r-infix ~(first form))
        :else
        (let [operator (second form)
              first-arg (first form)
              others (rest (rest form))] ; ...
          `(~operator
            (r-infix ~first-arg)
            (r-infix ~others)))))

(meditations
  "Macros are like functions created at compile time"
  (= "Hello, Macros!" (hello "Macros!"))

  #_"I can haz infix?"
  #_(= 10 (infix (9 + 1)))

  "Remember, these are nothing but code transformations"
  (= '(+ 9 1) (macroexpand '(infix (9 + 1))))

  "You can do better than that - hand crafting FTW!"
  (= '(* 10 2) (macroexpand '(infix-better (10 * 2))))

  "Things don't always work as you would like them to... "
  (= '(+ 10 (2 * 3)) (macroexpand '(infix-better ( 10 + (2 * 3)))))

  #_"Really, you don't understand recursion until you understand recursion"
  #_(= 36 (r-infix (10 + (2 * 3) + (4 * 5)))))
