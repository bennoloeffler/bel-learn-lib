(ns bel-learn-chapters.x-170-missionary-rx
  (:require [missionary.core :as m]
            [bel-learn-chapters.x-290-profiling :as prof]
            [clojure.string :as str])
  (:import [missionary Cancelled]))

  ;; TODO learn this https://github.com/leonoel/missionary
  ;; TODO GUI-Example "cpipe" - provide as example for missionary open source
  ;; https://clojureverse.org/t/missionary-new-release-with-streaming-support-design-notes/4510
  ;; VIDEO from Leo ... https://www.reddit.com/r/Clojure/comments/rn0wcx/functional_effect_and_streaming_systems_in/?utm_source=share&utm_medium=ios_app&utm_name=iossmf

  ;; https://github.com/ribelo/praxis
  ;; https://nextjournal.com/dustingetz/missionary-flow-tutorial-%E2%80%93peter-nagy


(defn run-10-ms
  "Runs CPU-intensive about 200 ms after warming up.
  Returns a huge number."
  [times] (reduce + (repeatedly (* 30 times) #(->> (range (+ 297 (rand-int 5)))
                                                   shuffle
                                                   vec
                                                   (mapv (fn [n] (reduce + (range (* n)))))
                                                   (reduce +)))))


(defn bigint?
  "Returns true if n is a BigInt"
  [n] (instance? clojure.lang.BigInt n))

(defn pow
  "(pow 2 3) = (* 2 2 2),
  even with very big numbers.
  Works with ints."
  [n x]
  (assert (or (int? n) (bigint? n)))
  (assert (or (int? x) (bigint? x)))
  (loop [nn 1N xx 0N]
    ;(println n x nn xx)
    (if (= x xx)
      nn
      (recur (* nn n) (inc xx)))))

(comment
  (pow 8 2)
  (pow 8 0)
  (pow 8 1)
  (prof/time+ (pow 97 89)))


(defn run-10-ms-no-mem
  "Runs CPU-intensive about 200 ms after warming up.
  Returns a huge number."
  [times]
  (let [pow (fn [n x] (loop [nn 1N xx 0N]
                        (if (= x xx)
                          nn
                          (recur (* nn n) (inc xx)))))]
    (dotimes [n (* 500 times)]
      (pow 97 89)))
  nil)


(prof/time+ (run-10-ms-no-mem 1))

(comment
  (prof/time+ (run-10-ms-no-mem 1))
  (prof/time+ (run-10-ms 1)))


(comment


  ;; ---------- TASK -----------
  ;; something that may be executed several times


  ; tasks
  (m/sleep 800) ; a sleep task
  (m/timeout (m/sleep 1000) 800) ; a timeout task

  ; sequential process
  (m/sp (println "one")
        :two)

  ; execute success
  ((m/sp (println "one")
         :two)
   #(println "success: " %)
   #(println "error" %))

  ; error
  ((m/sp (println "one")
         (throw (ex-info "failed" {:data "data"})))
   #(println "success: " %)
   #(println "error" %))

  ((m/sp "Hello") #(println "success: " %)
                  #(println "error" %))

  ;; cancel
  (def a-task (m/sleep 3000 :done))
  (def cancel (a-task #(println :ok %) (fn [_] (println :KO))))
  (cancel)



  ((m/sp (str (m/? (m/sleep 900 "Hi "))
              (m/? (m/sleep 100 "there!"))))
   #(println "success: " %)
   #(println "error" %))

  ; compose and wait for result by m/?
  (def example-task (m/sp (println "one")
                          (str (m/? (m/sleep 800 "Benno last and "))
                               (m/? (m/sleep 200 "Sabine first")))))

  (example-task #(println "success: " %)
                #(println "error" %))

  ;; clj only - OS-tasks needed!
  ;; start without continuation functions
  (m/? (m/sp :hello))
  (m/? example-task)
  (m/? (m/sp (println "Let's take a nap...")
             (str (m/? (m/sleep 900 "Hi "))
                  (m/? (m/sleep 100 "there!")))))

  (m/? (m/via m/blk (Thread/sleep 5000) :done))
  (m/? (m/via m/cpu (map #(reduce + (range %))
                         (shuffle (range 10000)))))

  ; read values sequentially
  (time (let [v1 (m/? (m/sp "hi"))
              v2 (m/? (m/sp (str (m/? (m/sleep 500 "Hi "))
                                 (m/? (m/sleep 500 "there!")))))]
          (printf "Read %s from %s\n" v1 v2)))

  ; read async - in parallel
  (time (let [[v1 v2] (m/? (m/join vector
                                   (m/sp (m/? (m/sleep 500 "Hi ")))
                                   (m/sp (m/? (m/sleep 500 "there!")))))]
          (printf "Read %s from %s%n" v1 v2)))


  ;; ---------- FLOW -----------


  ;; https://nextjournal.com/dustingetz/missionary-flow-tutorial-%E2%80%93peter-nagy
  (defn hello
    "This flow delivers on value on deref.
     n = function without arguments, the notifier to the consumer
     t = function without arguments, the terminator to the consumer
     @ = transfer
     (call) = cancel"
    [n t]
    (n)
    (reify
      clojure.lang.IDeref
      (deref [_] (t) "Hello world!")
      clojure.lang.IFn
      (invoke [_] (println "canceled"))))

  (assert
    ( = (m/? (m/reduce conj hello))
        ["Hello world!"]))

  (defn myreduce [rf flow]
    (let [notified? (atom false)
          terminate? (atom false)
          process (flow #(reset! notified? true) #(reset! terminate? true))]
      (loop [ret (rf)]
        (if @terminate?
          ret
          (if @notified?
            (do (reset! notified? false)
                (recur (rf ret @process)))
            (throw (ex-info "bad" {})))))))

  (assert
    ( = (myreduce conj hello)
        ["Hello world!"]))


  ;flow: produce values
  (m/seed [1 2 3])
  (def zip-flow (m/zip vector (m/seed (range 3)) (m/seed [:a :b :c])))
  (m/eduction (map inc) (m/seed [1 2 3]))

  (m/ap (println (m/?> (m/seed [1 2]))))

  ; use flows

  (let [a-flow (m/seed (range 4))
        a-task (m/reduce conj a-flow)]
    (m/? a-task))

  (let [a-flow (m/ap (println (m/?> (m/seed [1 2]))))
        a-task (m/reduce conj a-flow)]
    (m/? a-task))

  ; flow with side effects, no return value
  (m/? (m/reduce
         (constantly nil); just to avoid returning a collection
         (m/ap (println (m/?> (m/seed [1 2]))))))


  ;; ---------- FLOW and TASK -----------


  ;; 8 * 200 ms sequential would result in about 1,6 sec
  ;; 8 threads in parallel (realistic 6 and some loss...) should be about 1,6 / 8 = 0,2 sec
  ;; needs 0,19 after warming up
  (prof/time+ (let [;; create a flow of values generated by asynchronous tasks
                    inputs (repeat 8 (m/via m/cpu (run-10-ms 20))) ;; a task has no identity, it can be reused
                    values (m/ap
                             (let [flow (m/seed inputs)     ;; create a flow of tasks to execute
                                   task (m/?> ##Inf flow)]  ;; from here, fork on every task in **parallel**
                               (m/? task)))                 ;; get a forked task value when available

                    ;; drain the flow of values and count them
                    [n sum] (m/? ;; tasks are executed, and flow is consume here!
                                (m/reduce (fn [[count sum] v]
                                              ;(println count sum)
                                              ;(assert (= "hi" v))
                                              [(inc count) (+ sum v)])
                                          [0 0] values))]
                (println "sum: "sum)))


  ;; 8 * 200 ms = 1,6 sec but takes only 0,8 after warming up
  (prof/time+ (let [values (repeatedly 8 #(run-10-ms 20))]
                (println "sum:" (reduce + values))
                #_:end))

  ; ---------- WATCH a continuous flow

  (def !input (atom 5))
  (def input-flow (m/watch !input))
  (def input-signal (m/signal input-flow))
  (def input-signal*2 (m/latest (fn [input] {:result (* 2 input) :input input})
                                input-signal) #_(m/latest + input-signal input-signal))
  (def input-task (m/reduce (fn [acc v]
                              (println "task got value: " v ", acc: " acc)
                              (+ acc (:result v)))
                            0
                            input-signal*2))
  ;; here, we start the party!
  (def cancel (input-task #(println "task finished with value: " %) ;; this never happens, because the flow does not terminate
                          (fn [x] (println :ERROR x))))
  ;; the task reacts
  (swap! !input inc)

  (cancel)


  :end)

;;
;;--------------------  OLD  ------------------------------------------------------
;;
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