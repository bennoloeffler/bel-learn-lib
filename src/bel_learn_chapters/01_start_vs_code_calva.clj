(ns bel-learn-chapters.01-start-vs-code-calva)

; https://grison.me/2020/04/04/starting-with-clojure/
; https://clojureverse.org/t/my-beginner-s-journey-week-august-16-22-2021/8087

; https://github.com/ClojureBridge/curriculum
; https://exercism.io/tracks/clojure
;;
;; If you know java or groovy, then this is for you...
;;

;; see: http://kimh.github.io/clojure-by-example

;;
;; TO GET UP AND RUNNING
;;
;; *** VS-CODE & Calva ***
;; install leiningen (management tool for clojure)
;; install vscode (is much easier to start than jetbrains idea)
;; install extension calva (adds clojure abilities to vscode)
;; install extension clj-kondo (adds clojure abilities to vscode)
;; install git (well... you will have it anyway)
;;
;; clone this repo.
;; start vscode and open the project directory (or type 'code .'  inside)
;; Press CTRL-ALT-C CTRL-ALT-J choose Leiningen (aka Jack-in - start a REPL)
;; Press CTRL-ALT-C Enter (read a source file into the REPL)
;; Press ALT-Enter (evaluate function at cursor)

;; https://grison.me/2020/04/04/starting-with-clojure/

;; a function call is done like this
;; (function-name arg1 arg2 ...)
;; this is a LIST, with the first expression interpreted
;; as a function call = println
;; and the rest as arguments = "hello, clojure"
;; TRY IT.
;; place the curser somewhere into the list below and
;; press ALT-Enter.

(comment
  (println "hello, clojure"))

;; you are in a namespace - there is a global variable *ns*
*ns*
;; evaluate it... every binding will go into the current namespace
;; starting a repl, the namespace us 'user'

;;
;; the concept of a list
;;

;; lists are in paranthesis.
;; the first expression is interpreted as function (from the clojure reader in REPL)
;; the rest is interpreted as arguments to that function
(+ 1 2 3 4 5)

;; but with ' (clojurists call it 'quoted') the list is not evaluated, but taken as data
'(+ 1 2 3 4 5)
'("word" true 5 3.14)
;(println (+ 1 2)) ; prints 3
;(println '(+ 1 2)) ; prints (+ 1 2)

;;
;; Equality
;;

(= 1 1)
(= 3 5)
(= 3.0 3) ; not equal...
(== 3.0 3) ; but: compare by same effect
(= '(1 2) '(1 2)) ; not compared by identity, but same content


;;
;; Local let bindings
;;

(let [x 5] (+ x 1))

(let [cats 3
      legs (* 4 cats)]
  (str legs " legs all together"))

(let [x 10
      x (* 6 x)] (+ x 100))
;x ;not bound outside!

;;
;; global bindings (AVOID! whenever possible)
;;
(def cats 5)

(let [cats 13 ; overwritten by local one
      eyes (* 2 cats)]
  (str eyes " eyes all together"))

cats ; => 5 (evaluation to final value)

;;
;; From names to values (symbol, namespace, variable, value)
;;

;; a real value (Symbol --> Variable --> Value)
(type 'cats) ; Symbol
;; => clojure.lang.Symbol

(resolve 'cats) ; Variable cats in namespace bel-learn-chapters.01-up-and-running
;; => #'bel-learn-chapters.01-up-and-running/cats

(eval 'cats) ; Value
;; => 5

;; a functions value (Symbol --> Variable --> Value)
(type '+) ; Symbol ;; => clojure.lang.Symbol
(resolve '+) ; Variable ;;;; => #'clojure.core/+
(eval '+) ;Value ;; => #function[clojure.core/+]

;;
;; types
;;

(type nil) ; void null nothing
(type true) ; java boolean: true false
(type 1) ; java long
(type 2.34) ; java double
(type "meow") ; java string

;;
;; truth: nil and false are falsy.
;; EVERYTHING else is truthy
;;
(boolean nil) ;; => false
(boolean false) ;; => false
(boolean 0) ;; => true
(boolean ()) ;; => true

;;
;; simple calculations
;;

(+ 1 2) ;; => 3
(* 3 2.0) ;; => 6.0

;;
;; funtions with unexpected many arguments
;; and nesting
;;
(str "some nums " 3 " " 4 " " 5) ;; => "some nums 3 4 5"
(+ 3 4 5) ; => 12    call + operator with operands 3, 4 and 5
(- 1000 1 2 3 4) ;; => 990

;;
;; with CTRL-Enter you may evaluate the
;; expression nearest to the cursor. Try it...
;;
(+ (- 5 1) (* 8 (+ 1 1))) ; => 20   nested calls
(/ 3 4) ; => 3/4   what?
(type (/ 3 4)) ; Ratio - a new data type...
(type 3/4) ; can be written like that

(+ 2) ; with one argument
(- 13)
(+) ; 0: even with zero argument
(*) ; 1

;; there are no statements! only expressions.
;; even a (println "something") is an expression.
;; it returns nothing = nil
;(println "print something but return nothing")

;;
;; first smell of a function
;;
(def mult2 (fn [number] (* number 2)))
(defn c*2 [n] (* n 2)) ;; exactly the same. very uncommon symbol - may contain...
(mult2 10)
(c*2 10)
nil
;"this last expression will be the result of run file (CTRL-ALT-C Enter in Calva) and printed in the REPL"
