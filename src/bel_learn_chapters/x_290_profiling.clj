(ns bel-learn-chapters.x-290-profiling
  (:require [criterium.core :as cc]))


;: ----------------------------------
;; for SIMPLE PROFILING
;: ----------------------------------

; https://clojure-goes-fast.com/kb/benchmarking/time-plus/

(let [time*
      (fn [^long duration-in-ms f]
        (let [^com.sun.management.ThreadMXBean bean (java.lang.management.ManagementFactory/getThreadMXBean)
              bytes-before                          (.getCurrentThreadAllocatedBytes bean)
              duration                              (* duration-in-ms 1000000)
              start                                 (System/nanoTime)
              first-res                             (f)
              delta                                 (- (System/nanoTime) start)
              deadline                              (+ start duration)
              tight-iters                           (max (quot (quot duration delta) 10) 1)]
          (loop [i 1]
            (let [now (System/nanoTime)]
              (if (< now deadline)
                (do (dotimes [_ tight-iters] (f))
                    (recur (+ i tight-iters)))
                (let [i'          (double i)
                      bytes-after (.getCurrentThreadAllocatedBytes bean)
                      t           (/ (- now start) i')]
                  (println
                    (format "Time per call: %s   Alloc per call: %,.0fb   Iterations: %d"
                            (cond (< t 1e3) (format "%.0f ns" t)
                                  (< t 1e6) (format "%.2f us" (/ t 1e3))
                                  (< t 1e9) (format "%.2f ms" (/ t 1e6))
                                  :else (format "%.2f s" (/ t 1e9)))
                            (/ (- bytes-after bytes-before) i')
                            i))
                  first-res))))))]

  (defmacro time+
    "Like `time`, but runs the supplied body for 2000 ms and prints the average
  time for a single iteration. Custom total time in milliseconds can be provided
  as the first argument. Returns the returned value of the FIRST iteration."
    [?duration-in-ms & body]
    (let [[duration body] (if (integer? ?duration-in-ms)
                            [?duration-in-ms body]
                            [2000 (cons ?duration-in-ms body)])]
      `(~time* ~duration (fn [] ~@body)))))


(comment

  (defn count-distribution []
    (doall (frequencies (map (fn [_] (rand-int 1000)) (range 10000))))
    [:NO-RESULT])

  (cc/quick-bench (count-distribution))

  ; or just
  ;
  (time+ (count-distribution)))




;; https://clojure-goes-fast.com/kb/profiling/clj-async-profiler/
;; use [com.clojure-goes-fast/clj-async-profiler "1.0.3"]
;; see: https://github.com/clojure-goes-fast/clj-async-profiler

(comment
  ;; understand graph
  ;; http://clojure-goes-fast.com/blog/

  ;; put that in your project.clj dev profile
  ;; :jvm-opts ["-Djdk.attach.allowAttachSelf"]

  (require '[clj-async-profiler.core :as prof])

  ;; Profile the following expression:
  (prof/profile (dotimes [i 10000] (reduce + (range i))))

  ;; The resulting flamegraph will be stored in /tmp/clj-async-profiler/results/
  ;; You can view the HTML file directly from there or start a local web UI:

  (prof/serve-ui 8080) ; Serve on port 8080

  nil)

