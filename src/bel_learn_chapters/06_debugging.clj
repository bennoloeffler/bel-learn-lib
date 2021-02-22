(ns bel-learn-chapters.06-debugging
  [:require
   [clojure.string :as str]
   [erdos.assert :as pa]  ;power-assert
   [taoensso.timbre :refer [spy error warn info debug]] ;logging
   [clojure.tools.trace :as trace :refer [dotrace trace-forms]]]) ;tracing

(defn debug-this [arg1 arg2]
  (let [from (min arg1 arg2) ; breakpoint may have conditions
        to (max arg1 arg2)
        start (+ arg1 arg2)]
    (->> (range from to 3) ; get range
         (map #(* % 3)) ; multiply each by 3
         (filter odd?) ; get only the odds
         (reduce #(- %1 %2) start)))) ; reduce by minus, starting at 1000

(comment (debug-this 155 25))

(defn ^:dynamic do-div [x y]
  (/ x y)) ; setting an exception breakpoint helps

(defn ^:dynamic calc [x y]
  (do-div x y))

; https://stackoverflow.com/questions/41946753/how-can-i-trace-code-execution-in-clojure
; https://github.com/clojure/tools.trace
(dotrace [calc do-div] (calc 5 6))
#_(trace-forms (+ 1 3) (* 5 6) (/ 1 0)) ;; To identify which form is failing

(comment (calc 10 0))

(let [pow 3 value 3]
  (loop [i pow res 1]
    (if (zero? i)
      res
      (recur (dec i) (* (doto res prn) value)))))

(defn pow [value pow]
  (loop [i pow res 1]
    (if (zero? i)
      res
      (recur (dec i) (* res value)))))
(pow 2 8)
(trace/trace [pow] (pow 2 8))

;;----------------------------------------
(require '[mate-clj.core :as mate])
;; https://github.com/AppsFlyer/mate-clj
;; DOES NOT WORK! SHIT!
(mate/d->> [:1 :2 :3 :4]
;(->> [:1 :2 :3 :4]
    shuffle
    ;(map #(str % "--")))
    str/join)
;;---------------------------------------

(def c (atom 0))
(->> [:1 :2 :3 :4]
     shuffle
     (#(do (println (swap! c inc) ": " %) %))
     (map str)
     (#(do (println (swap! c inc) ": " %) %))
     (map second)
     (#(do (println (swap! c inc) ": " %) %))
     (partition 2)
     (#(do (println (swap! c inc) ": " %) %))
     (map clojure.string/join)
     (#(do (println (swap! c inc) ": " %) %)))

;(pa/assert (= 1 (count ( range 1 9))))
;(pa/examine (= 1 (count ( range 1 9))))
(pa/assert (= 8 (count ( range 1 9))))

(pa/examine (->> [:1 :2 :3 :4]
                 (shuffle)
                 (map #(str % "--"))
                 (doall) ;see lazy ones
                 (clojure.string/join)))


;; https://github.com/ptaoussanis/timbre
(taoensso.timbre/info "This will print")
(taoensso.timbre/debug "error")
(info (Exception. "Oh no - this is a shituation") "data 1" 1234)
(error (Exception. "Oh no - this is a shituation") "data 1" 1234)
(defn my-calc [a b c] (* a b c))
(spy (my-calc 1 2 3))



(->> [:1 :2 :3 :4] ; spy does work
     shuffle
     spy
     (map #(str % "--"))
     vec ; doall does not work?
     spy
     str/join
     spy)

(defmacro dbg [body]
  `(let [body# ~body]
     (println "dbg:" '~body "=>" body#)
     body#))

(dbg (+ 1 2))

(->> [:1 :2 :3 :4]
     dbg
     shuffle
     dbg
     (map #(str % "--"))
     dbg
     str/join
     dbg)
