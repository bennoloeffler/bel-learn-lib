(ns bel-learn-chapters.x-160-rx
  (:require [seesaw.core :refer :all])
  (:import (java.util.concurrent ScheduledThreadPoolExecutor
                                 TimeUnit)
           (clojure.lang PersistentQueue)))

;; beicon cljs wrapper to js-rx: http://funcool.github.io/beicon/latest/user-guide.html
;; mini-re-frame https://dawranliou.com/tags/clojure/
;; https://dawranliou.com/blog/event-driven-clojurescript-front-end-with-core-async/
;; https://gist.github.com/staltz/868e7e9bc2a7b8c1f754
;; https://vdoc.pub/documents/clojure-reactive-programming-3bpf8l6ge0pg
;; Part 3
;; chapter: Building a stock market monitoring application
;; slides to book: https://www.slideshare.net/borgesleonardo/functional-reactive-programming-compositional-event-systems
;; threads
;; CODE for Book: https://github.com/PacktPublishing/Hands-On-Reactive-Programming-with-Clojure-Second-Edition

(def pool (atom nil))

(defn init-scheduler [num-threads]
  (reset! pool (ScheduledThreadPoolExecutor. num-threads)))

(defn run-every [pool millis f]
  (.scheduleWithFixedDelay pool
                           f
                           0
                           millis TimeUnit/MILLISECONDS))

(defn stop-pool []
  (println "thread stops pool...")
  (Thread. #((.shutdown @pool) (println "done")))) ; does that work?


; average logic

(defn roll-buffer [buffer num buffer-size]
  (let [buffer (conj buffer num)]
    (if (> (count buffer) num)
      (pop buffer)
      buffer)))

(defn avg [numbers]
  (float (/ (reduce + numbers)
            (count numbers))))

(defn make-running-avg [buffer-size]
  (let [buffer (atom PersistentQueue/EMPTY)]
    (fn [n]
      (swap! buffer roll-buffer n buffer-size)
      (avg @buffer))))

(def running-avg (make-running-avg 5))

;; business logic

(defn share-price [company-code]
  (Thread/sleep 200)
  (rand-int 1000))

(defn create-frame []
  (native!)
  (let [main-frame       (frame :title "Stock price monitor"
                                :width 200 :height 100
                                :on-close :dispose)
        price-label      (label :text "Price: -" :id :price-label)
        moving-avg-label (label :text "AVG: -" :id :moving-avg-label)]

    (config! main-frame
             :content (border-panel
                        :north price-label
                        :center moving-avg-label
                        :border 10))))


(defn worker [f]
  (let [prize (share-price "XYZ")]
     (text! (select f [:#price-label]) (str "Prize: " prize))
     (text! (select f [:#moving-avg-label]) (str "AVG: " (running-avg prize)))))

(defn -main [& args]
  (let [f (create-frame)]
    (show! f)
    (.addShutdownHook (Runtime/getRuntime)
                      (stop-pool))
    (init-scheduler 1)
    (run-every @pool 250
               #(invoke-now (worker f)))
    f))

(comment
  (def f (-main)))