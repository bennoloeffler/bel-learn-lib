(ns bel-learn-chapters.x-168-rx-core-async
  (:refer-clojure :exclude [filter map deliver])
  (:require [clojure.core.async :refer [go-loop <! >! <!! >!! close! chan mult map> filter> tap go timeout]])
  (:import [clojure.lang IDeref]))


;; TODO
;; switch to Transducers: https://dzone.com/articles/reducers-transducers-and-coreasync-in-clojure

;; LINKS
;; Chapter 5 ... "A minimla CES Famework" from Clojure Reactive Programming, Leonardo Borges
;; see https://github.com/chourave/reagi/tree/release-0.11.0
;; https://blog.jayway.com/2014/09/16/comparing-core-async-and-rx-by-example/
;; https://www.slideshare.net/borgesleonardo/functional-reactive-programming-compositional-event-systems
(add-tap println)


(defprotocol IBehavior
  (sample [b interval]
    "Turns this behavior into an EventStream from the sampled values at the given interval"))

(defprotocol IEventStream
  (map [s f]
    "Returns a new EventStream containing
    the result of applying f to the values of s")
  (filter [s pred]
    "Returns a new EventStream containig only those values,
    which returned true when pred was applied")
  (flatmap [s f]
    "Takes a function f from values in s to a new EventStream.
    Returns a EventStream containing all values
    from all underlying streams combined.")
  (deliver [s value]
    "Delivers a value to the stream s")
  (deliver-sync [s value]
    "Delivers a value to the stream s - do in go or thread block...")
  (completed? [s]
    "returns true, if this stream has stopped emitting values.")
  (zip [s s-other]
       "Returns a new event stream, that delivers values of both streams alternating."))

(defprotocol IObservable
  (subscribe [obs f]
    "Register a callback to be invoked when the
    underlying source changes.
    Returns a token the subscriber can use to
    cancel the subscription."))

(defprotocol IToken
  (dispose [tk]
    "Called when the subscriber isn't interested in receiving more items"))


;;; IMPL


(deftype Token [ch]
  IToken
  (dispose [_]
    (close! ch)))


(declare event-stream)

(defn view-buf-es [es & [name]]
  (tap> (str "buf in ch in es: " name (.buf (.buf (.channel es))))))

(defn view-buf-ch [ch & [name]]
  (let [b (.buf (.buf ch))]
    (tap> (str "buf in ch: " name b))))

(deftype EventStream [channel multiple completed]

  IEventStream

  (map [_ f]
    (let [out (map> f (chan))]                              ; map> ? clojure.core/map --> Transducer?
      (tap multiple out)
      (event-stream out)))

  (deliver [_ value]
    (if (= value ::complete)
      (do (reset! completed true)
          (go (>! channel value)
              (close! channel)))
      (go
        (tap> (str "going to deliver: " value))
        (>! channel value)
        (tap> (str "delivered: " value)))))
        ;(view-buf-ch channel))))

  (deliver-sync [_ value]
    (if (= value ::complete)
      (do (reset! completed true)
          (>!! channel value)
          (close! channel))
      (do
        ;(println "going to deliver sync")
        (>!! channel value))))
        ;(println "delivered synced " value))))


  (flatmap [_ f]
    (let [es  (event-stream)
          out (chan)]
      (tap multiple out)
      (go-loop []
        (when-let [a (<! out)]
          (let [mb (f a)]
            (subscribe mb (fn [b]
                            (deliver es b)))
            (recur))))
      es))

  (filter [_ pred]
    (let [out (filter> pred (chan))]                        ; filter> ? clojure.core/filter --> Transducer?
      (tap multiple out)
      (event-stream out)))

  (completed? [_] @completed)

  (zip [this es-other]
       (let [es  (event-stream)]
             ;out (chan)]
         ;(tap multiple out)
         (go-loop []
           (let [v1 (<! (.channel this))
                 v2 (<! (.channel es-other))]
             (tap> (str "v1=" v1 ", v2=" v2))
             (when (and v1 v2)
               (deliver-sync es [v1 v2])
               (recur))))
         es))



  IObservable

  (subscribe [_ f]
    (println "subscribe")

    (let [out (chan)]
      (tap multiple out)
      (go-loop []
        (println "read val..")
        (let [value (<! out)]
          (println "subs val = " value)
          (when (and value (not= value ::complete))
            (f value)
            (recur))))
      (Token. out))))

(declare es-from-interval)

(deftype Behavior [f]
  IBehavior
  (sample [_ interval]
    (es-from-interval interval (f) (fn [& args] (f))))
  IDeref
  (deref [_]
    (f)))

(defmacro behavior [& body]
  `(Behavior. #(do ~@body)))


(defn event-stream
  "Creates and returns a new event stream. You can can provide an existing chan as source"
  ([]
   (event-stream (chan)))
  ([ch]
   (let [multiple  (mult ch)
         completed (atom false)]
     (->EventStream ch multiple completed))))

(defn es-from-zip
  [es1 es2]
  (let [es (event-stream (chan))]
    ;out1 (chan)
    ;out2 (chan)]
    ;(tap (.multiple es1) out1)
    ;(tap (.multiple es2) out2)
    (go-loop []
      (tap> "try zip reading channels")
      (view-buf-es es1)
      ;(tap> (str "buf c2: " (.buf (.buf (.channel es2)))))
      (let [v1 (<! (.channel es1))
            v2 (<! (.channel es2))]
        (tap> (str "v1=" v1 " v2=" v2))
        (if (and v1 v2)
          (do
            (println "DELIVER: v1=" v1 " v2=" v2)
            (deliver-sync es [v1 v2])
            ;(deliver es v2)
            (recur))
          (println "stoppped es-from-zip"))))
    es))


(defn es-from-range
  "creates an event stream which delivers (range 0 n)"
  [n]
  (let [es (event-stream (chan n))]
    (go (doseq [n (range n)]
          ;(println "range:" n)
          (deliver-sync es n)))
    es))


(defn es-from-interval
  "Creates and returns a new event stream which
  emits values at the given interval. If no other
  arguments are given, values start at 0
  and increment by 1 at each delivery"
  ([msecs]
   (es-from-interval msecs 0 inc))
  ([msecs seed inc-fun]
   (let [es (event-stream)]
     (go-loop [timeout-ch (timeout msecs)
               next-value seed]
       (when-not (completed? es)
         (<! timeout-ch)
         (deliver-sync es next-value)
         (recur (timeout msecs) (inc-fun next-value))))
     es)))

(comment
  (do

    ;; make it concurrent save
    (add-tap println)

    (def es1 (event-stream))
    (subscribe es1 #(tap> (str "es1 normal: " %)))

    (def es2 (map es1 #(* 2 %)))
    (subscribe es2 #(tap> (str "es2 double: " %)))

    (def es3 (filter es1 even?))
    (subscribe es3 #(tap> (str "es3 even: " %)))


    (Thread/sleep 100)
    (deliver es1 10)
    (Thread/sleep 100)
    (deliver es1 40)
    (Thread/sleep 100)
    (deliver es1 5))

  (do
    (def es1 (event-stream))
    (subscribe es1 #(tap> (str "es1 normal: " %)))
    (deliver es1 {:a 42}))

  (do
    (def es1 (event-stream))
    (def es2 (flatmap es1 es-from-range))
    (subscribe es1 #(prn "es1 emitted: " %))
    (subscribe es2 #(prn "es2 emitted: " %))
    (deliver es1 4))

  (do
    (def es1 (es-from-interval 50))
    (def es1-token (subscribe es1 #(prn "Got: " %)))
    (Thread/sleep 550)
    (dispose es1-token))

  (do
    (def time-behavior (behavior (System/nanoTime)))
    @time-behavior
    (def time-stream (sample time-behavior 150))
    (def tok (subscribe time-stream #(prn "t=" %)))
    (Thread/sleep 1000)
    (dispose tok))

  (do
    (def es1 (es-from-range 10))
    (Thread/sleep 1000)
    (subscribe es1 #(println "range: " %)))

  (do
    ; TODO WORKS!
    (def es1 (es-from-interval 500))
    (def es2 (es-from-interval 500))
    (def es-zipped (es-from-zip es1 es2))
    (def tok (subscribe es-zipped #(println "zipped: " %))))


  (do
    ; TODO es-zipped does not work ? see ref and dosync
    (def es1 (es-from-range 50))
    (def t (subscribe es1 #(println "sub:" %)))
    (view-buf-es es1)
    (def es2 (es-from-range 50))
    (def es-zipped (es-from-zip es1 es2)))
  (do
    (def tok (subscribe es-zipped #(println "zipped: " %)))
    (Thread/sleep 1000)
    (println "going to dispose")
    (dispose tok)
    ; stop the intervals
    (deliver es1 ::complete)
    (deliver es2 ::complete))

  (do

    (defn put-n [ch n]
      (go
        (doseq [v (range n)]
          (>! ch v))))

    (defn take-all [ch f]
      (go-loop []
        (if-let [v (<! ch)]
          (do
            (f v)
            (recur))
          (println "take over..."))))

    (defn zip-all [ch1 ch2 f]
      (go-loop []
        (let [v1 (<! ch1)
              v2 (<! ch2)]
          (if (and v1 v2)
            (do (f [v1 v2])
                (recur))
            (println "zip over...")))))

    (def c1 (chan))
    (put-n c1 10)
    (take-all c1 #(prn %))
    (Thread/sleep 100)
    (close! c1)

    (def c1 (chan 2))
    (def c2 (chan 2))
    (put-n c1 12)
    (put-n c2 20)
    (Thread/sleep 100)
    (zip-all c1 c2 #(prn %))
    (Thread/sleep 100)
    (close! c1)))



  ;(do-sync))



(def es1 (es-from-interval 100))
(def t (subscribe es1 #(println "res: " %)))
(Thread/sleep 1000)
(dispose t)

