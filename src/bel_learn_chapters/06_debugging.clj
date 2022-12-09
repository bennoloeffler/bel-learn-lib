(ns bel-learn-chapters.06-debugging
  [:require
    [clojure.string :as str]
    [clojure.pprint :refer (pprint)]
    [puget.printer :refer (cprint)]
    [erdos.assert :as pa]                                   ;power-assert
    [taoensso.timbre :refer [spy error warn info debug set-min-level!]]    ;logging
    [clojure.tools.trace :as trace :refer [dotrace trace-forms]] ;tracing
    [debux.core :refer :all]                                ; dbg
    [hashp.core :refer :all]])                              ;#p

; http://www.futurile.net/2020/05/16/clojure-observability-and-debugging-tools/

(set! *print-length* 20)

;;---------------------------------------
;; dbg und #p
;;---------------------------------------

(comment

  (use 'hashp.core)
  ;hashp
  (/ 10 #p (/ (- 12 10) (+ 10 1)))

  ;debux
  (dbg (->> (all-ns)
            (shuffle)
            (take 20)
            (map ns-name)
            sort
            (partition 4)))

  ;erdos
  (pa/examine (->> (range 10)
                   (map inc)
                   doall                                    ; because lazy result from map
                   (reduce +))))

;;---------------------------------------
;; breakpoint cursive idea / exception breakpoint / enable when other hit...
;;---------------------------------------


(defn debug-this [arg1 arg2]
  (let [from (min arg1 arg2)                                ; breakpoint may have conditions
        to (max arg1 arg2)
        start (+ arg1 arg2)]
    (->> (range from to 3)                                  ; get range
         (map #(* % 3))                                     ; multiply each by 3
         (filter odd?)                                      ; get only the odds
         (reduce #(- %1 %2) start))))                       ; reduce by minus, starting at 1000

(comment (debug-this 155 25))

;;---------------------------------------
;; TRACE and MIX #p and trace
;;---------------------------------------

(defn ^:dynamic do-div [x y]
  (/ x #p y))                                               ; setting an exception breakpoint helps

(defn ^:dynamic calc [x y]
  (let [xx (inc x) yy (dec y)]
    (do-div xx yy)))

; https://stackoverflow.com/questions/41946753/how-can-i-trace-code-execution-in-clojure
; https://github.com/clojure/tools.trace
(comment
  (dotrace [calc do-div] (calc 4 7))
  (trace-forms (+ 1 3) (* 5 6) (/ 1 1)))                    ;; To identify which form is failing

(comment (calc 10 1))                                       ; set an exception breakpoint...

(comment                                                    ; printing something in between
  (let [pow 3 value 3]
    (loop [i pow res 1]
      (if (zero? i)
        res
        (recur (dec i) (* #p res value))))))
;(recur (dec i) (* (doto res prn) value))

;;---------------------------------------
;; TRACE
;;---------------------------------------

(defn pow [value pow]
  (loop [i pow res 1]
    (if (zero? i)
      res
      (recur (dec i) (* res value)))))

(defn mult-pow [to]
  (->> (range 1N (inc to)) (map #(pow % %))))

(comment                                                    ; tracing
  (trace/trace-vars bel-learn-chapters.06-debugging/pow)
  (mult-pow 20N))

;;----------------------------------------
;; MATE-CLJ
;;---------------------------------------

(require '[mate-clj.core :as mate])
;; https://github.com/AppsFlyer/mate-clj

(comment ;; DOES NOT WORK! SHIT!
  (mate/d->> [:1 :2 :3 :4]
             shuffle
             (map str))

  ; but this does
  (dbg (->> [:1 :2 :3 :4]
            shuffle
            (map #(str % "--"))
            str/join))

  ; this too
  (def m {:body "flow test"})
  (mate/d-> m
            :body                                           ;step #1
            (clojure.string/upper-case)                     ;step #2
            (clojure.string/reverse)))                      ;step #3)

;;---------------------------------------
;; POWER ASSERTS
;;---------------------------------------

;(pa/assert (= 1 (count ( range 1 9))))
;(pa/examine (= 7 (count ( range 1 9))))
(pa/assert (= 8 (count (range 1 9))))

(comment
  (pa/examine (->> [:1 :2 :3 :4]
                   (shuffle)
                   (map #(str % "--"))
                   (doall)                                  ;see lazy ones
                   (clojure.string/join)))
  (pa/examine (->> [:1 :2 :3 :4] shuffle str/join)))

;;---------------------------------------
;; TIMBRE LOGGING
;; https://github.com/ptaoussanis/timbre
;;---------------------------------------

(comment
  (taoensso.timbre/merge-config! {:min-level :info})        ; :level debug does nothing!
  (taoensso.timbre/info "This will print")
  (taoensso.timbre/debug "error"))
;(info (Exception. "Oh no - this is a shituation") "data 1" 1234)
;(error (Exception. "Oh no - this is a shituation") "data 1" 1234))

(defn my-calc [a b c] (* a b c))
(comment (spy (my-calc 1 2 3)))
(comment
  (set-min-level! :debug)
  (->> [:1 :2 :3 :4]                                        ; spy does work
       spy
       shuffle
       spy
       (map #(str % "--"))
       vec                                                  ; doall does not work?
       spy
       str/join
       spy))

(pa/examine
  (->> [:1 :2 :3 :4]
       (shuffle)
       (map #(str % "--"))
       (clojure.string/join)))

(dbg (->> [:1 :2 :3 :4]
          (shuffle)
          (map #(str % "--"))
          (clojure.string/join)))

;;---------------------------------------
;; OWN MACRO
;;---------------------------------------


(defmacro dbg-bel [body]
  `(let [body# ~body]
     (println "-------- dbg-bel --------------")
     (cprint ['~body :--> body#])
     (println)
     body#))


(comment
  (dbg-bel (+ 1 2))

  (->> [:1 :2 :3 :4]
       shuffle
       dbg-bel
       (map #(str % "--"))
       dbg-bel
       str/join
       dbg-bel))

(comment (dbg-bel (my-calc 1 2 3)))


;;---------------------------------------
;; even simpler
;;---------------------------------------

(def c (atom 0))
(comment
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
       (#(do (println (swap! c inc) ": " %) %))))
