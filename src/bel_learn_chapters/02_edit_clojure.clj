(ns bel-learn-chapters.02-edit-clojure)


;; PAREDIT in VSCODE(calva) und IDEA(cursive)
;;
;; to master inline evaluation of expressions the brackets,
;; paranthesis and curls, you may have a look at this...

;; CURSIVE
;; ALT-SHIFT-R to switch to this namespace
;; ALT-SHIFT-L to load namespace
;; so click to the 1 and ALT-SHIFT-P
;; ALT-SHIFT-P execute current form (or selected)

;; CALVA
;; place the cursor to the 1 and press CTRL-Enter.
;; So: click inside an expression and CTRL-Enter evaluates that.
(comment
  (+ 1 (/ 4 2) (dec 9)))
;; place the cursor to the 6, press CTRL-W until (+ 6 7 8 9) is marked.
;; then press CTRL-Enter.
;; you should see this: => 30
;; you may be able to evaluate an marked sub-expressions inline
(comment
  (+ (* 40 50) (+ 6 7 8 9)))
;; CTRL-ALT-P in idea

;; place the curser behind the 6
;; press CTRL-arrow-left <--  7 times
;; you move the right bracket to left... until its end is reached
;; this means: press CTRL-arrow-right --> 5 times
;; everything is the same as before
;; Then again: press CTRL-arrow-right --> 5 times
;; NOW YOU WONT GET THE FINAL paran back... with just CTRL - <--
;; you have to place the curser behind the bracket after the 9
;; and again press CTRL-arrow-left.
(+ (* 40 50) (+ 6 7 8 9))
(+ (/ 7 8) (dec 9))
;; SHIFT-ALT-J in idea

;; barf and slurp (J to left, K to right)
;; SHIFT-ALT = forward
;; CTRL-ALT = backward

; CTRL-arrow moves the right paran to the next valid position
; CTRL-SHIFT arrow does the same with the paran on the left side
; for more see: https://calva.io/paredit/


;;
;; Parinfer is a totally different approach...
;;
(comment
  (let [x 3 y 1]
    (->> (map inc [x y 7 1])
         sort
         (#(do (prn %) %))
         (partition 2)))) ;just comment with trialing semicolon


; ENTF and SPACE move the whole block in parinfer mode)
(defn a [b]
  (if (b)
    (inc b)
    (println b)))

;;
;; Comments
;;

; comment
;; comment
#_(ignore the next expression) (str "but not the next")
(comment
 (+ 100 #_(+ 200 300) (+ 1))
 (+ 5 11))
