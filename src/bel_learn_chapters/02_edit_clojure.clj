(ns bel-learn-chapters.02-edit-clojure)


;; IDEA(cursive)
;;
;; to master inline evaluation of expressions the brackets,
;; paranthesis and curls, you may have a look at this...

;; CURSIVE
;; ALT-SHIFT-R to switch to this namespace
;; ALT-SHIFT-L to load namespace and execute current form (or selected)
;; so click to the 1 and ALT-SHIFT-P

;; CALVA
;; place the cursor to the 1 and press CTRL-Enter.
;; So: click inside an expression and CTRL-Enter evaluates that.
(comment
  (+ 1 (/ 4 2) (dec 9)))

;; place the cursor to the 6, press CTRL-W until (+ 6 7 8 9) is marked.
;; then press CTRL-Enter or CTRL-ALT-P
;; you should see this: => 30
;; you may be able to evaluate an marked sub-expressions inline
(comment
  (+ (* 40 50) (+ 6 7 8 9)))

;; SLURP and BARK symbols into out of the current brackets
;; place the curser behind the 6
;; press SHIFT-ALT-J  7 times
;; you move the right bracket to left... until its end is reached
;; this means: press SHIFT-ALT-K 7 times
;; everything is the same as before
;; Then again: press SHIFT-ALT-K 2 times
;; NOW YOU WONT GET THE FINAL paran back...
;; by 2 x SHIFT-ALT-J
;; Experiment a little bit with this
;;
;; SHIFT-ALT-J (move right paranthesis more left) or BARF symbols to the right
;; SHIFT-ALT-K (move right paranthesis more right) or SLURP symbols from right
;; STRG-ALT-J (move left paran more left) or SLURP symbols from left
;; STRG-ALT-K (move left paran more right) or BARF symbols to the left
;; or short:
;; barf and slurp (J to left, K to right)
;; SHIFT-ALT = forward
;; CTRL-ALT = backward

(+ (* 40 50) (+ 6 7 8 9))
(+ (/ 7 8) (dec 9))


;;
;; Parinfer is a totally different approach...
;; SWITCH ON PARINFER in CURSIVE
;;
(comment
  (let [x 3 y 1]
    (->> (map inc [x y 7 1])
         sort
         (#(do (prn %) %))
         (partition 2)))) ;just comment with trialing semicolon


; ENTF and SPACE move the whole block in parinfer mode. TAB moves one line. SHIFT-TAB reverses TAB.
(defn calc [num]
  (if (num)
    (inc num)
    (println "b=nil or b=false")))

;; BACKSPACE doese probably not what you expect! Try it.

;;
;; Comments
;;

; comment
;; comment
#_(ignore the next expression) (str "but not the next")
(comment
 (+ 100 #_(+ 200 300) (+ 1))
 (+ 5 11))

;; threading macros - make code more readable
;;STRG-ALT-, STRG-ALT-.
;; place curser here ->>(range 10)
;; start pressing repeatedly STRG-ALT-,

(+ 1 (reduce + (sort (map inc (range 10)))))
(+ 1 (reduce + (sort (map inc (range 10)))))