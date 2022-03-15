(ns belib.util)

;
; recursion with memoize of intermediate steps
; BUT anyway: stack overflow
;

(defn Y-mem [f]
  (let [mem (atom {})]
    (#(% %)
      (fn [x]
        (f #(if-let [e (find @mem %&)]
              (val e)
              (let [ret (apply (x x) %&)]
                (swap! mem assoc %& ret)
                ret)))))))


(defmacro defrecfn [name args & body]
  `(def ~name
     (Y-mem (fn [foo#]
              (fn ~args (let [~name foo#] ~@body))))))


(defrecfn fib [n]
          (if (<= n 1)
            n
            (+' (fib (- n 1)) (fib (- n 2)))))

(time (fib 400))
(time (fib 1400))
(time (fib 2400))
(time (fib 3400))
(time (fib 4400))
(time (fib 5400))
(time (fib 6400))
