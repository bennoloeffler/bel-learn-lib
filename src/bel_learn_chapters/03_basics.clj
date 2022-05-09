(ns bel-learn-chapters.03-basics
  [:require clojure.repl])


(let [cats 3
      legs (* 4 cats)]
  (str legs " legs all together"))

((fn [x] (inc x)) 4)

(let [twice (fn [x] (* 2 x))]
  (+ (twice 1)
     (twice 3)))

;; define a function with name bmi
;; and the imput parameters
;; weight-in-kg (NOT: weightInKg. Clojure uses other idiom)
;; heigth-in-cm

(defn bmi
  "calculates the body mass index"
  [weight-in-kg heigth-in-cm]
  (float
   (/ weight-in-kg
      (*
       (/ heigth-in-cm 100)
       (/ heigth-in-cm 100)))))


;; call the function
(bmi 91 176)

(defn bmi-results
  "based on bmi-value from bmi, gives hints, what that means"
  [bmi-value]
  (cond
    (< bmi-value 20) (format "BMI = %.1f: you could or should increase weight..." bmi-value)
    (<= 20 bmi-value 25) (format "BMI = %.1f: healthy weight" bmi-value)
    (<= 25 bmi-value 31) (format "BMI = %.1f: you should decrease weight..." bmi-value)
    (> bmi-value 31) (format "BMI = %.1f: you are definitely too fat!" bmi-value)))

;(println "the result..." (bmi-results (bmi 93 176)))

;(bmi-results 31) fails ... needs float for %.1f
(bmi-results 31.1)


;;
;; what is a funtion?
;;

(fn [x] (* 2 x)) ; create a function (2 * x) without a name and do not call it... USELESS
#(* 2 %) ;exactly the same - a anonymous function with #(%) as abbrevation
((fn [x] (* 2 x)) 5) ; call function with arg 5
(#(* 2 %) 5) ;call with abbevation
(def f*2 (fn [x] (* 2 x))) ; now we have a function and bind them to the name "f*2"
(defn f+2 [x] (+ 2 x)) ; this is just an abrevation (defn = def + fn)
(f*2 5)
(f+2 18)
(map f*2 [1 2 3 4]) ; map function f1 to all elements of a vector
(map #(* 2 %) [1 2 3 4]) ; here we can use an anonymous function...

;; the old map filter reduce game...
(reduce #(- %1 %2) 1000 ;; 4. finally reduce
        (filter odd? ;; 3. filter odd?
                (map (partial * 3) ;; 2. map with *3
                     (range 1 20 3)))) ;; 1. range

;; this is called threading macro ->> makes it much more readable
(->> (range 1 20 3) ; get range => (1 4 7 10 13 16 19)
     (map #(* % 3)) ; multiply each by 3 ;; => (3 12 21 30 39 48 57)
     (filter odd?) ; get only the odds ;; => (3 21 39 57)
     (reduce #(- %1 %2) 1000)) ; reduce by minus, starting at 1000

;;
;; arity  varargs
;; AND
;; doc meta source supers (explore the code)
;;

(defn add
  "adding two numbers"
  [x y]
  (+ x y))

(add 3 4)

;(clojure.repl/doc add)
(meta #'add)
(supers (type add))

(defn half
  "half or half of x"
  ([]  1/2)
  ([x] (/ x 2)))

(half 10)
(half)


(defn vargs
  [x y & more-args]
  {:x    x
   :y    y
   :more more-args})

(comment
  (vargs 1 2 3 4 5 6)
  (vargs 1 2)
  (+ 1 (reduce + (sort (shuffle (map inc (range 10))))))
  (conj #{1 2 3} 1 1 1 2 3 4)
  (disj #{1 2 3 4} 1)
  (remove #(= 2 %) [1 2 3 4 2])
  (apply max [1 2 3 4]))

;(clojure.repl/source +)


