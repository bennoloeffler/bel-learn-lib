(ns bel-learn-chapters.x-160-rx
  (:require [seesaw.core :refer :all])
  (:import (java.util.concurrent ScheduledThreadPoolExecutor
                                 TimeUnit)))

;; https://vdoc.pub/documents/clojure-reactive-programming-3bpf8l6ge0pg
;; Part 3
;; chapter: Building a stock market monitoring application

;; threads

(def pool (atom nil))

(defn init-scheduler [num-threads]
  (reset! pool (ScheduledThreadPoolExecutor. num-threads)))

(defn run-every [pool millis f]
  (.scheduleWithFixedDelay pool
                           f
                           0
                           millis TimeUnit/MILLISECONDS))

(defn shutdown [pool]
  (println "Shutting down scheduler…")
  (.shutdown pool))


;; business logic

(defn share-price [company-code]
  (Thread/sleep 200)
  (rand-int 1000))



(defn create-frame  []
  (native!)
  (let [main-frame (frame :title "Stock price monitor"
                          :width 200 :height 100
                          :on-close :dispose)
        price-label (label :text "Price: -" :id :price-label)]

    (config! main-frame :content price-label)))

(defn -main [& args]
 (let [f (create-frame)]
  (show! f)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(shutdown @pool)))
  (init-scheduler 1)
  (run-every @pool 500
             #(->> (str "Price: " (share-price "XYZ"))
                   (text! (select f [:#price-label]))
                   invoke-now))))

(comment
  (-main))