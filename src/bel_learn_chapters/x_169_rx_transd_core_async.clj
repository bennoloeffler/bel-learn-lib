(ns bel-learn-chapters.x-169-rx-transd-core-async
  "BELs little reactive framework. Based on core.async.
  "
  (:refer-clojure :exclude [filter map deliver])
  (:require [clojure.core.async :refer [timeout pipeline go-loop <! >! >!! close! chan mult tap go]]
            [bel.util.log :refer :all])

  (:import [clojure.lang Atom IDeref]))

;; TODO use this https://github.com/leonoel/missionary
;; alternative to learn: https://github.com/theleoborges/imminent
;; alternative to learn: https://github.com/chourave/reagi/tree/release-0.11.0
;; Transducers: https://dzone.com/articles/reducers-transducers-and-coreasync-in-clojure
;; core.async & transducers https://www.youtube.com/watch?v=096pIlA3GDo
;; Chapter 5 ... "A minimla CES Famework" from Clojure Reactive Programming, Leonardo Borges

;; TODO - constructors
;;      - put final version in own package bel.util.rx (including tests)


(defn to-test [])



;;; Interfaces

(defprotocol IBehavior
  (sample [b interval]
    "Turns this behavior into an EventStream from the sampled values at the given interval"))

(defprotocol IEventStream
  (start-subscriptions [eventstream]
    "There are streams that wait for starting their subscriptions.
    They are starting to call their subsribed callbacks when start is called.
    E.g. es-from Atom.")
  (deliver [eventstream value]
    "Delivers a value to the stream s 'manually'.
    To mark the channel as completed and close it, (deliver eventstream ::completed).")
  (attach-to-col [eventstream col]
    "Delivers all values of col to the stream s.
    Stream stays open and may be used afterwards.")
  (completed? [eventstream]
    "returns true, if this stream has stopped emitting values.")
  (complete [eventstream]
    "Stops it! It stops forwarding events. Channel is closed.")
  (trans [eventstream xform] ; TODO Change name to eduction?
    "Returns a new stream containing the result of applying xform to the stream")
  (flatmap [eventstream f]
    "TODO: describe")
  #_(attach-to-col [eventstream col]
      "Delivers all values of col to the stream s.
    Stream stays open and may be used afterwards.")
  (attach-to-atom [eventstream a]
    "The stream gets its values from changes of an atom.")
  (attach-to-interval [eventstream msecs f]
    "The stream gets its values from calling f every msecs.")
  (attach-to-channel [eventstream ch]
    "The stream gets its values from reading a channel."))

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

(deftype EventStream [name channel multiple completed started-ch] ;started-atom]

  IEventStream

  (start-subscriptions [_]
    (go (>! started-ch true)
        (l :start name)
        (close! started-ch)))

  (deliver [_ value]
    (go
      (l :deliver "queue val " value)
      (<! started-ch)
      (if (= value ::complete)
        (do (reset! completed true)
            (l :close "received ::complete es=" name)
            (close! channel))
        (do
          (l :deliver value)
          (>! channel value)))))

  (completed? [_] @completed)

  (complete [_] (reset! completed true) (close! channel))

  (trans [_ xform]
    (let [into-trans-es (chan)
          es            (event-stream into-trans-es "trans-es")
          from-source   (chan)]

      ;(mult channel)
      (tap multiple from-source)
      (pipeline
        ;threads
        4
        into-trans-es
        xform
        from-source)

      es))

  (flatmap [_ f]
    (let [es  (event-stream)
          out (chan)]
      (tap multiple out)
      (go-loop []
        (when-let [a (<! out)]
          (let [mb (f a)]
            (subscribe mb (fn [b]
                            (l :flatmap "delivering " b)
                            (deliver es b)))
            (recur))))
      es))

  (attach-to-col [es col]
    (go
      (<! started-ch)
      (doseq [value col]
        (l :deliver-col value)
        (>! channel value)))
    es)


  (attach-to-atom [es a] ; TODO check if behavour
    (go
      (<! started-ch)
      (deliver es @a) ; TODO: watch for completed and remove add-watch
      (add-watch a :key (fn [key atom old-state new-state] (deliver es new-state))))
    es)

  (attach-to-interval [es msecs f]
    (go
      (<! started-ch)
      (go-loop []
        (deliver es (f))
        (<! (timeout msecs))
        (when-not (completed? es) (recur))))
    es)

  (attach-to-channel [es ch])



  IObservable

  (subscribe [_ f]
    (let [out (chan)]
      ;m (mult channel)]
      (go
        (<! started-ch)
        (l :subscribe "delvery of subscription started...")
        (tap multiple out)
        (go-loop []
          (let [value (<! out)]
            (l :subscribe "v=" value)
            (when (and value (not= value ::complete))
              (f value)
              (l :subscribe "delivered: " value)
              (recur)))))

      (Token. out))))


(declare es-from-interval)

(deftype Behavior [f]
  IBehavior
  (sample [behaviour interval]
    (attach-to-interval (event-stream) interval f))
  IDeref
  (deref [_]
    (f)))

(defmacro behavior [& body]
  `(Behavior. #(do ~@body)))


#_(defmulti es-from (fn [t] (type t)))

#_(defmethod es-from Atom [t]
    (let [es (event-stream (chan 1) "atom-es" true)]
       (deliver es @t)
       (add-watch t :key (fn [key atom old-state new-state] (deliver es new-state)))
       es))



(defn event-stream
  "Creates and returns a new event stream. You can can provide an existing chan as source"
  ([]
   (event-stream (chan) "raw-es" false))
  ([ch]
   (event-stream ch "chan-es" false))
  ([ch name]
   (event-stream ch name false))
  ([ch name wait-for-start]
   (let [multiple-ch (mult ch)
         completed   (atom false)
         started-ch  (chan 1)]
     (if-not wait-for-start
       (do (>!! started-ch true)
           (close! started-ch)
           (l :start "start done! es=" name))
       (l :start "waiting for start-subscriptions! name of es=" name))
     (->EventStream name ch multiple-ch completed started-ch))))


