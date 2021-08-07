(ns bel-learn-chapters.90-threads
  (:gen-class)
  (:require
    [clojure.data.json :as json]
    [puget.printer :as puget :refer [cprint]]
    [clojure.core.async
     :as a
     :refer [>! <! >!! <!! go chan buffer close! thread
             alts! alts!! timeout go-loop]]))

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

;;
;; ------ reading quotes from network -------
;;


(defn read-rand-quote []
  (let [rand-q-api-url "https://zenquotes.io/api/random"
        quote-json (slurp rand-q-api-url)
        [{q :q a :a}] (json/read-str
                        quote-json
                        :key-fn keyword)]
    {:q q :a a}))


(defn read-n-quotes-parallel [n]
  (->> (map (fn [n] (future (read-rand-quote)))
            (range n))
       (map deref)))


(defn print-quote [{quote :q author :a}]
  (println "")
  (println quote)
  (println "--- " author " --- ")
  (println "")
  {:q quote :a author})


(comment
  ;(def rand-q-api-url "https://zenquotes.io/api/random")
  ;(def quote-json (slurp rand-q-api-url))
  ;(def quote-clj (json/read-str quote-json :key-fn keyword))
  ;(println quote-clj)
  (read-rand-quote)
  (def f (future (read-rand-quote)))
  (print-quote @f)
  (map print-quote (read-n-quotes-parallel 3))
  m)


;;
;; ---------- promise ----------
;;

(comment
  (def my-promise (promise))
  (deliver my-promise (+ 1 2))
  @my-promise)


(comment
  (let [my-promise (promise)
        all (doall (for [n (range 3)]
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
