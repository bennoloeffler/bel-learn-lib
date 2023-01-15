(ns bel-learn-chapters.90-threads
  (:require
    [clojure.data.json :as json]
    [puget.printer :as puget :refer [cprint]]
    [clojure.core.reducers]
    [clojure.core.async
     :as a
     :refer [>! <! >!! <!! go chan buffer close! thread
             alts! alts!! timeout go-loop]]
    [debux.core :refer :all] ; dbg
    [hashp.core :refer :all]))


; https://purelyfunctional.tv/guide/clojure-concurrency/
; https://ericnormand.me/guide/clojure-concurrency

;;
;; ----------   future ----------
;;

(comment
  (do
    ; starts a thread immediately
    (future (Thread/sleep 4000)
            (println "I'll print after 4 seconds"))
    (println "I'll print immediately")))


(comment
  (let [result (future (println "Thread DOING the calc.")
                       (Thread/sleep 2000)
                       100)]
    ;(reduce + (range 1000000000)))]
    (println "Thread WAITING for calc...")
    (println @result)
    (println "No waiting, no calc: CACHED!")
    (println @result)))


(comment
  (deref (future (Thread/sleep 100) :real-result) 1000 :default-value)
  (deref (future (Thread/sleep 1000) :real-result) 100 :default-value))


(comment
  (let [future-result (future (Thread/sleep 100))]
    (println (realized? future-result))
    @future-result
    (println (realized? future-result))))

;;
;; ----------   delay ----------
;;

(comment
  ; starts a thread only after force
  (def delayed-start (delay
                       (let [name "Benno"]
                         (println "processing name: " name)
                         name)))
  ;same as deref / @
  (force delayed-start)
  ; second deref: same as future - cached, no more execution
  (deref delayed-start))


(comment
  (do
    (let [notify-callback (delay
                            (future (Thread/sleep 100)
                                    (println "3 this is the callback")
                                    (Thread/sleep 500)))]

      (future (Thread/sleep 100)
              (println "1 network work")
              ;(Thread/sleep 500)
              (force notify-callback)
              ;(Thread/sleep 500)
              (println "2 network work")))

    (println "thread goes on...")))

(comment
  (let [callback-data (promise)]
    (future (let [data @callback-data]
              (println "Callback: With some data:" data)))
    (println "doing main task")
    (Thread/sleep 1000)
    (deliver callback-data {:a "x" :b "z"})))

(comment
  (let [p1 (promise)
        f1 (future (Thread/sleep 2000) (deliver p1 1))
        p2 (promise)
        f2 (future (Thread/sleep 2000) (deliver p2 2))
        p3 (promise)
        f3 (future (Thread/sleep 2000) (deliver p3 3))]
    (println @p1 @p2 @p3)))

;;
;; ------ reading quotes from network -------
;;


(defn read-rand-quote []
  (let [start          (System/currentTimeMillis)
        rand-q-api-url "https://zenquotes.io/api/random"
        quote-json     (slurp rand-q-api-url)
        [{q :q a :a}] (json/read-str
                        quote-json
                        :key-fn keyword)
        end            (System/currentTimeMillis)]
    {:q q :a a :msec (- end start)}))


(defn read-n-quotes-parallel [n]
  (->> (map (fn [n] (future (read-rand-quote)))
            (range n))
       (map deref)))


(defn print-quote [{quote :q author :a msec :msec}]
  (println "")
  (println quote)
  (println "--- " author ", " msec " msec --- ")
  (println "")
  {:q quote :a author :msec msec})


(comment
  ;(def rand-q-api-url "https://zenquotes.io/api/random")
  ;(def quote-json (slurp rand-q-api-url))
  ;(def quote-clj (json/read-str quote-json :key-fn keyword))
  ;(println quote-clj)
  (read-rand-quote)
  (def f (future (read-rand-quote)))
  (print-quote @f)
  (do
    (time (cprint (map print-quote (read-n-quotes-parallel 3))))
    (cprint "thread finished")))




;;
;; ---------- promise ----------
;;

(comment
  (def my-promise (promise))
  (deliver my-promise (+ 1 2))
  @my-promise)


(comment
  (let [my-promise (promise)
        all        (doall (for [n (range 3)]
                            (future
                              (let [q (read-rand-quote)]
                                (deliver
                                  my-promise
                                  q)
                                q))))]
    (println "first finisher:")
    (cprint (deref my-promise 4000 "timeout"))
    (println)
    (println "all...")
    (cprint (map deref all))))


;;
;; core.async chan, go, thread, put! ( >! >!! chan value) take! (<! chan), timeout(-channel)
;;
(comment
  (def echo-chan (chan 2))
  (go (println (<! echo-chan)))
  (>!! echo-chan ["ketchup" :and "pommes"]))


(comment
  (go-loop [seconds 20
            to-wait (inc (rand-int 5))]
    (if (> seconds 0)
      (do
        (println "waiting for "
                 to-wait
                 " remaining now: " seconds ", then: "
                 (- seconds to-wait))
        (<! (timeout (* 1000 to-wait)))
        (recur (- seconds to-wait) (inc (rand-int 5))))
      (println "END"))))


(comment
  (do (def hi-chan (chan))
      ;(println hi-chan)
      (doseq [n (range 100)]
        (go (>! hi-chan (str "hi " n)))))

  (for [n (range 20)] (<!! hi-chan)))

(comment
  (do
    (def queue (chan 10000))
    (thread (loop [n 1] (Thread/sleep 50)
                        (>!! queue n)
                        (if (< n 200)
                          (recur (inc n))
                          (close! queue))))
    (thread (loop [] (Thread/sleep (rand-int 100))
                     (let [v (<!! queue)
                           _ (println v)]
                       (if v
                         (recur)
                         (println "END")))))))

(defn rand-str
  "random string of characters from a to z of length len"
  [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))



(defn find-pattern [pattern content]
  (let [matcher (re-matcher pattern content)]
    (loop [count 0]
      (if (nil? (re-find matcher))
        count
        (recur (inc count))))))

(defn hard-task [pattern]
  (find-pattern pattern (rand-str 10000000)))

(comment
  (time (do
          (+ (hard-task #"ABC") (hard-task #"CBA")
             (hard-task #"XY") (hard-task #"YX")
             (hard-task #"VVV") (hard-task #"ZZZ"))))



  (time (do
          (let [threads-all   [(future (+ (hard-task #"XY") (hard-task #"YX")))
                               (future (+ (hard-task #"ABC") (hard-task #"CBA")))
                               (future (+ (hard-task #"VVV") (hard-task #"ZZZ")))]
                total         @(reduce #(atom (- (deref %1) (deref %2))) threads-all)
                the-first     (promise)
                threads-first (map deref [(future (deliver the-first (+ (hard-task #"XY") (hard-task #"YX"))))
                                          (future (deliver the-first (+ (hard-task #"ABC") (hard-task #"CBA"))))
                                          (future (deliver the-first (+ (hard-task #"VVV") (hard-task #"ZZZ"))))])]
            (+ total (deref the-first 10000 -1000000000N))))))

(defn run-task-group [task-group]
  ())


