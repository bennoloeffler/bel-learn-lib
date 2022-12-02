(ns bel-learn-chapters.x-170-missionary-rx
  (:require [missionary.core :as m]
            [clojure.string :as str])
  (:import [missionary Cancelled]))

  ;; TODO learn this https://github.com/leonoel/missionary
  ;; TODO GUI-Example "cpipe" - provide as example for missionary open source
  ;; https://clojureverse.org/t/missionary-new-release-with-streaming-support-design-notes/4510
  ;; VIDEO from Leo ... https://www.reddit.com/r/Clojure/comments/rn0wcx/functional_effect_and_streaming_systems_in/?utm_source=share&utm_medium=ios_app&utm_name=iossmf

  ;; https://github.com/ribelo/praxis
  ;; https://nextjournal.com/dustingetz/missionary-flow-tutorial-%E2%80%93peter-nagy


  ; this is a reactive computation, the println reacts to input changes
(defn missionary-example-01 []
      (let [input    (atom 2)
            main     (m/reactor
                       (let [>x (m/signal! (m/watch input)) ; continuous signal reflecting atom state
                             >y (m/signal! (m/latest + >x >x))] ; derived computation, diamond shape
                         (m/stream! (m/ap (println "bel: " (m/?< >y))))))
            disposer (main #(prn ::success %) #(prn ::crash %))]
        (Thread/sleep 40)
        (swap! input inc)
        (swap! input inc)
        (swap! input inc)
        (swap! input inc)
        (swap! input inc)
        (swap! input inc)
        (swap! input inc)
        (disposer)))

; using tasks
(defn missionary-example-02 []
  (let [task-1           (m/sp
                           (m/? (m/sleep 1000))
                           (println "first task")
                           (* 10 10 10))

        task-2           (m/sp
                           (m/? (m/sleep 1000))
                           (println "second task")
                           (* 21 2))

        sequential-tasks (m/sp
                           (m/? task-1)
                           (m/? task-2))
        parallel-tasks   (m/join vector task-1 task-2)]

    (time (m/? sequential-tasks))
    (time (m/? parallel-tasks))))

(defn missionary-example-03 []
  (let [task-1         (m/sp
                         (m/? (m/sleep 3000)) ; runs longer - paused
                         (println "first task")
                         (* 10 10 10))

        task-2         (m/sp
                         (m/? (m/sleep 1000))
                         (println "second task")
                         (/ 21 0)) ; EXCEPTION... fails earlier

        parallel-tasks (m/join vector task-1 task-2)]

    ;kill first task after fail of second
    (time (try (m/? parallel-tasks)
               (catch Exception e (str "cought " e))))))

(defn missionary-example-04 []
  (let [task-1         #(time (do
                                (Thread/sleep 2000) ; runs longer - blocked
                                (print "first task  -  ")
                                (* 10 10 10)))

        task-2         #(time (let [sum (reduce + (range 1000000000))]
                                (print "second task  -  ")
                                sum)) ; runs longer - cpu

        parallel-tasks (m/join vector (m/sp (task-1)) (m/sp (task-2)))
        blk-cpu-tasks  (m/join vector (m/via m/blk (task-1)) (m/via m/cpu (task-2)))]

    (m/? parallel-tasks)
    (m/? blk-cpu-tasks)))

(defn missionary-example-05 []
  (let [input (m/seed (range 10))
        sum   (m/reduce + input)]
    (m/? sum)
    (m/? (m/reduce conj (m/eduction (partition-all 4) input)))))

(defn missionary-example-06 []
  (let [flow (m/ap
               (println (m/?> (m/seed ["Hello" "World" "!"])))
               (m/? (m/sleep 300))
               (long (rand 100)))]

    (m/? (m/reduce conj flow))))

(defn missionary-example-07 []
  (let [now      #(System/currentTimeMillis)
        start    (now)
        dur      #(- (now) start)
        dur-str  #(str " (d=" (dur) ")")

        debounce (fn [delay flow]
                   (m/ap (let [x (m/?< flow)]
                           ;(println "   DBNC: going to start 50 for " x (dur-str))
                           (try (m/? (m/sleep delay x))
                                (catch Cancelled _ (do
                                                     (println "   DBNC: ---> canceled: " x (dur-str))
                                                     (m/amb>)))))))
        clock    (fn [intervals]
                   (m/ap (let [i (m/?> (m/seed intervals))]
                           (m/? (m/sp
                                  ;(println "CLCK: going to sleep and return: " i (dur-str))
                                  (m/? (m/sleep i i))
                                  (println "CLCK: returning: " i (dur-str))
                                  i)))))]
    ; simple obervation: when the successor
    ; takes longer than 50ms, the value comes through.
    ; otherwise, it will be "preemted" by  ?< the Cancelled sleep
    ; 67 will be killed by 34
    ; 34 by 18
    ; 18 by 9
    ; 99 by 37
    ; interessting are values of ABOUT 50....
    (m/? (->> (clock [24 79 67 34 18 9 99 37 99 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59])
              (debounce 50)
              (m/reduce conj)))))

(defn missionary-example-08 []
  (time (let [flow (m/ap (let [ms (m/?= (m/seed [400 300 300 300 300 500 300 300 300 300 300 300 300]))]
                           ; this blocks?
                           ;(time (clojure.core/reduce + (range (* 10000 ms))))))]
                           (m/? (m/sleep ms ms))))]

          (m/? (m/reduce conj flow)))))

(defn missionary-example-09 []
  (let [input    [1 4 9 10]
        main     (m/reactor
                   (let [>x (m/signal! (m/seed input)) ; continuous signal reflecting atom state
                         >y (m/signal! (m/latest + >x >x))] ; derived computation, diamond shape
                     (m/stream! (m/ap (println "bel: " (m/?< >y))))))
        disposer (main #(prn ::success %) #(prn ::crash %))]
    (disposer)))

(comment
  (missionary-example-01) ; reactive
  ; task
  (missionary-example-02) ; sequential and parallel tasks
  (missionary-example-03) ; one task failing, othere stopped
  (missionary-example-04) ; joining blocking, cpu-intensive and normal tasks
  ; flow
  (missionary-example-05) ; simple flow through transducer
  (missionary-example-06) ; ambigous process: fork flows
  (missionary-example-07) ; cancel a fork - example dedup
  (missionary-example-08) ;
  nil)

(println (missionary-example-09))

(->> (range 100)
     (map str)
     (map keyword))
(keyword "5")

(comment

  ;; ######  TASKS  #######

  (def task-1 (m/sp
                (println "one")
                :two)) ; sequential process
  (def nap (m/sleep 1000))
  (def timeout-nap (m/timeout (m/sleep 1000) 100)) ; long running tasks may be stopped by timeout

  ; async : with continuation function for success and error
  (task-1
    #(println "Hello" %)
    #(println :KO %))

  (m/? task-1) ;; execute it, clj ONLY

  ; compose task
  (def task-nap (m/sp (println "Let's take a nap...")
                      (str (m/? (m/sleep 90 "Hi "))
                           (m/? (m/sleep 1000 "there!")))))

  (do ;; cancel does not work ???
    (def a-task (m/sleep 15000 :done))
    (def cancel (a-task #(println :ok %) (fn [_] (println :KO))))
    (cancel)) ; (on stdout) :KO)

  (do
    (def cancel (task-nap #(println :ok %) (fn [_] (println :KO))))
    (cancel))

  ;; ######  FLOWS  #######

  (def input (m/seed (range 10)))
  (def sum-task (m/reduce + input))
  (m/? sum-task)
  (m/? (m/reduce conj (m/eduction (partition-all 4) input)))

  ;; ambiguous evaluation (fork)
  (def hello-world
    (m/ap
      (let [x (m/?> (m/seed ["Hello," "world" "and" "Benno"]))
            y 13]
        (m/? (m/sleep 1000 (do (println x) (repeat y x)))))))
  (m/? (m/reduce conj hello-world))

  nil)