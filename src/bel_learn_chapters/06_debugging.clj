(ns bel-learn-chapters.06-debugging
  [:require [erdos.assert :as pa]  ;power-assert
            [taoensso.timbre :refer :all]]) ;logging
(defn debug-this [arg1 arg2]
  (let [from (min arg1 arg2) ; breakpoint may have conditions
        to (max arg1 arg2)
        start (+ arg1 arg2)]
    (->> (range from to 3) ; get range
         (map #(* % 3)) ; multiply each by 3
         (filter odd?) ; get only the odds
         (reduce #(- %1 %2) start)))) ; reduce by minus, starting at 1000

(comment (debug-this 155 25))

(defn do-div [x y]
  (/ x y)) ; setting an exception breakpoint helps

(defn calc [x y]
  (do-div x y))

(comment (calc 10 0))

(let [pow 3 value 3]
  (loop [i pow res 1]
    (if (zero? i)
      res
      (recur (dec i) (* (doto res prn) value)))))


;;----------------------------------------
(require '[mate-clj.core :as mate])
;; https://github.com/AppsFlyer/mate-clj
;; DOES NOT WORK! SHIT!
;(mate/d->> [:1 :2 :3 :4]
(->> [:1 :2 :3 :4]
     shuffle
     (map #(str % "--"))
     clojure.string/join)
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

(pa/assert (= 1 (count ( range 1 9))))
(pa/examine (= 1 (count ( range 1 9))))

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
