(ns bel-learn-chapters.x-169-rx-transd-core-async
  (:refer-clojure :exclude [filter map deliver])
  (:require [clojure.core.async :refer [pipeline go-loop <! >! close! chan mult map> filter> tap go]]))


;; TODO
;; switch to Transducers: https://dzone.com/articles/reducers-transducers-and-coreasync-in-clojure
;; https://www.youtube.com/watch?v=096pIlA3GDo

;; LINKS
;; Chapter 5 ... "A minimla CES Famework" from Clojure Reactive Programming, Leonardo Borges
;; see https://github.com/chourave/reagi/tree/release-0.11.0


;;; Interfaces

; TODO implement
(defprotocol IBehavior
  (sample [b interval]
    "Turns this behavior into an EventStream from the sampled values at the given interval"))

(defprotocol IEventStream
  #_(map [s f]
      "Returns a new EventStream containing
    the result of applying f to the values of s")
  #_(filter [s pred]
      "Returns a new EventStream containig only those values,
    which returned true when pred was applied")
  #_(flatmap [s f]
      "Takes a function f from values in s to a new EventStream.
    Returns a EventStream containing all values
    from all underlying streams combined.")
  (deliver [s value]
    "Delivers a value to the stream s")
  (completed? [s]
    "returns true, if this stream has stopped emitting values."))

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

(deftype EventStream [channel multiple completed]

  IEventStream

  #_(map [_ f]
      (let [out (map> f (chan))] ; map> ? clojure.core/map --> Transducer?
        (tap multiple out)
        (event-stream out)))

  (deliver [_ value]
    (if (= value ::complete)
      (do (reset! completed true)
          (go (>! channel value)
              (close! channel)))
      (go (>! channel value))))

  #_(flatmap [_ f]
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

  #_(filter [_ pred]
      (let [out (filter> pred (chan))] ; filter> ? clojure.core/filter --> Transducer?
        (tap multiple out)
        (event-stream out)))
  (completed? [_] @completed)

  IObservable

  (subscribe [this f]
    (let [out (chan)]
      (tap multiple out)
      (go-loop []
        (let [value (<! out)]
          (when (and value (not= value ::complete))
            (f value)
            (recur))))
      (Token. out))))


(defn event-stream
  "Creates and returns a new event stream. You can can provide an existing chan as source"
  ([]
   (event-stream (chan)))
  ([ch]
   (let [multiple  (mult ch)
         completed (atom false)]
     (->EventStream ch multiple completed))))

(comment
  (do

    (add-tap println) ;; make it concurrent

    (def es1 (event-stream))
    (subscribe es1 #(tap> (str "es1 normal: " %)))

    (def es2 (map es1 #(* 2 %)))
    (subscribe es2 #(tap> (str "es2 double: " %)))

    (def es3 (filter es1 even?))
    (subscribe es3 #(tap> (str "es3 even: " %))))
  (do
    (Thread/sleep 100)
    (deliver es1 10)
    (Thread/sleep 100)
    (deliver es1 40)
    (Thread/sleep 100)
    (deliver es1 5))

  (do
    (def es1 (event-stream))
    (subscribe es1 #(tap> (str "es1 normal: " %)))
    (deliver es1 17)))

(comment
  (def ca (chan 1))
  (def cb (chan 1))

  (pipeline
      4               ; thread count, i prefer egyptian cotton
      cb              ; to
      (clojure.core/filter even?)  ; transducer
      ca)              ; from

  (do
    (doseq [i (range 10)]
      (go (>! ca i)))
    (go-loop []
      (println (<! cb))
      (recur))))
