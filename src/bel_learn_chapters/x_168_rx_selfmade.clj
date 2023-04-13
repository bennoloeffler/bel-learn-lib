(ns bel-learn-chapters.x-168-rx-selfmade
  (:require [clojure.core.async  :as  async
             :refer  [go  go-loop  chan  <!  >!  timeout
                      map>  filter>  close!  mult  tap  untap]])
  (:refer-clojure  :exclude  [filter  map  deliver])
  (:import  [clojure.lang  IDeref]))


; TODO
; - create macro for error handling that encapsulates try-catch when adding to a channel
; - replace map> filter> ...

;; ----- HELPERs ---------------------------------------

(defn prn [& args]
  (.println *out* (apply str (interpose \space args))))

;; ----- Handling of exceptions in core.async ----------

(defmacro >?
  "Replaces >!
  Just puts the exception into channel,
  if one is thrown by body.
  Use together with <?"
  [ch body]
  `(>! ~ch (try ~body (catch Throwable e# e#))))

(defn throw-err [e]
  (when (instance? Throwable e) (throw e))
  e)

(defmacro <?
  "Replaces <!
  Read value and throws when reading a
  Throwable from channel.
  Use together with >?"
  [ch]
  `(throw-err (<! ~ch)))

(comment

  (defn get-data []
    (throw (Exception. "Bad	things happen!")))

  (defn process []
    (let [result (chan)]
      ;;	do	some	processing…
      (go (>? result (get-data)))
      result))

  (go (try (let [result (<? (->> (process "data")
                                 (map> #(* % %))
                                 (map> #(prn %))))]
             (prn "result	is:	" result))
           (catch Exception e
             (prn "Oops, an error happened! We better do something about it here!"))))

  nil)


;; ----- observable stream and token abstraction --------

(defprotocol IObservable

  (subscribe [obs f f-err]
    "Register  a callback to be invoked when
    the underlying source changes.
    f-err is called, when a Throwable is delivered.
    Returns a token the subscriber can use to
    cancel the subscription."))


(defprotocol IBehavior

  (sample [b interval]
    "Turns this Behavior into an EventStream
    from the sampled values at the given interval"))


(defprotocol  IEventStream

  (map [s f]
    "Returns a new stream containing the
    result of applying f to the values in s")

  (filter [s pred]
    "Returns	a	new	stream	containing	the	items	from	s
    for	which	pred	returns	true")

  (flatmap [s f]
    "Takes a function f from values in s
    to a new EventStream.
    Returns an EventStream containing values
    from all underlying streams combined.")

  (deliver [s value]
    "Delivers a value to the stream s")

  (completed? [s]
    "Returns true if this stream has stopped
    emitting values. False otherwise.")

  (take [s number]
    "Returns a new stream that delivers only the
    next number events and then stops delivering
    events.")

  (zip [s other-es]
    "zips this and other-es event stream.
    [valueFromThis1 valueFromOther1]
    [valueFromThis2 valueFromOther2]"))





(defprotocol IToken

  (dispose [tk]
    "Called when the subscriber isn't
    interested in receiving	more items"))


(deftype Token [ch]

  IToken

  (dispose [_]
    (close! ch)))


(declare  event-stream)


(deftype  EventStream  [channel  multiple  completed]

  IEventStream

  (map  [_  f]
    (let  [out  (map>  f  (chan))]
      (tap  multiple  out)
      (event-stream  out)))

  (filter  [_  pred]
    (let  [out  (filter>  pred  (chan))]
      (tap  multiple  out)
      (event-stream  out)))

  (deliver  [_  value]
    (prn "deliver: " value)
    (if  (=  value  ::complete)
      (do  (reset!  completed  true)
           (go  (>?  channel  value)
                (close!  channel)))
      (do
        ;(println "deliver:" value)
        (go  (>?  channel  value)))))

  (flatmap  [_  f]
    (let  [es  (event-stream)
           out  (chan)]
      (tap  multiple  out)
      (go-loop  []
        (when-let  [a  (<?  out)]
          (let  [mb  (f  a)]
            (subscribe  mb  (fn  [b]
                              (deliver  es  b))
                        nil)
            (recur))))
      es))

  (take [s number]
    (let  [es  (event-stream)
           count (atom number)
           fire (fn [val]
                  (if (> @count 0)
                    (do
                      (deliver es val)
                      (swap! count dec))
                    (close! (.channel es))))] ;; same as using (dispose tok)

      (subscribe s fire nil)
      es))

  (zip [s es-other]
    (let  [es  (event-stream)]
      (go-loop  []
        (let  [a  (<?  channel)
               b  (<?  (.channel es-other))]
           (when (and a b)
             (deliver  es  [a b])
             (recur))))
      es))

  (completed?  [_]  @completed)

  IObservable

  (subscribe  [this  f err-f]
    (let  [out  (chan)]
      (tap  multiple  out)
      (go-loop  []
        (let  [value  (<!  out)]
          (prn "out:" value)
          (when  (and  value  (not=  value  ::complete))
            (if (instance? Throwable value)
              (if err-f
                (do
                  (prn "call err-f value")
                  (err-f value))
                (prn "ignoring err. To receive, provide err-f: " value))
              (do
                (f  value)
                (recur))))))
      (Token.  out))))

(defmacro deliver-ex [es body]
  `(deliver ~es (try ~body (catch Throwable e# (deliver ~es e#)))))


(defn  event-stream
  "Creates and returns a new event stream.
  You can optionally provide an existing
  core.async channel as the source for the new stream."
  ([]
   (event-stream  (chan)))
  ([ch]
   (let  [multiple    (mult  ch)
          completed  (atom  false)]
     (EventStream.  ch  multiple  completed))))


(defn  range-es
  "Creates a new event stream that delivers
  all numbers of range n."
  [n]
  (let  [es  (event-stream  (chan  n))]
    (doseq  [n  (range  n)]
      (deliver  es  n))
    es))

(defn range-es-throw
  "Creates a new event of range n.
  Last element throws exception."
  [n]
  (let [es (event-stream (chan n))]
    (doseq [n (range (dec n))]
      (deliver es n))
    (deliver-ex es (/ n 0))
    es))

;; -------------- BEHAVIOUR -----------------------

(defn  from-interval
  "Creates and returns a new event stream which
  emits values at the given interval.
  If no other arguments are given, the values
  start at 0 and increment by one at each delivery.
  If given seed and succ it emits seed and
  applies succ to seed to get the next value.
  It then applies succ to the previous result and so on."
  ([msecs]
   (from-interval  msecs  0  inc))
  ([msecs  seed  succ]
   (let  [es  (event-stream)]
     (go-loop  [timeout-ch  (timeout  msecs)
                value  seed]
       (when-not  (completed?  es)
         (<!  timeout-ch)
         (deliver  es  value)
         (recur  (timeout  msecs)  (succ  value))))
     es)))

(deftype Behavior [f]
  IBehavior
  (sample  [_ interval]
    (from-interval  interval  (f)  (fn  [&  args]  (f))))
  IDeref
  (deref [_]
    (f)))

(defmacro behavior [& body]
  `(Behavior. #(do ~@body)))

(comment

  ;*** eventstream, subscribe, deliver

  (def  es1  (event-stream))
  (def tok1 (subscribe  es1  #(prn  "es1 emitted: "  %) nil))
  (deliver  es1  10)

  ;*** map

  (def  es2  (map  es1  #(*  2  %)))
  (def tok2 (subscribe  es2  #(prn  "es2 (*2) emitted: "  %) nil))
  (deliver  es1  20)
  (deliver  es2  2)

  ;*** filter

  (def  es3  (filter  es1  even?))
  (def tok3 (subscribe  es3  #(prn  "es3 (even?) emitted: "  %) nil))
  (deliver es1 1)
  (deliver es1 2)

  ;*** flatmap

  (def  es4  (event-stream))
  (def  es5  (flatmap  es4  range-es))
  (subscribe  es4  #(prn  "es4 emitted: "  %) nil)
  (subscribe  es5  #(prn  "es5 emitted: "  %) nil)
  (deliver  es4  2)

  ; behaviour

  (def  es6  (from-interval  500))
  (def  es6-token  (subscribe  es6  #(prn  "Got: "  %) nil))
  (dispose  es6-token)

  (def  time-behavior  (behavior  (System/nanoTime)))
  @time-behavior

  (def  time-stream  (sample  time-behavior  200))
  (def  token (subscribe  time-stream  #(prn  "Time is "  %) nil))
  (dispose  token)

  ; exercise: take
  (def  es7  (from-interval  500))
  (def token1 (subscribe  es7  #(prn  "es7: "  %) nil))
  (do ; at once
    (def  take-es  (take  es7  5))
    ;;(subscribe  take-es  #(prn  "Take values: "  %))
    (def token2 (subscribe  take-es  #(prn  "Take values: "  %) nil)))
  (dispose token1)
  (dispose token2)

  ; exercise: zip
  (def  es8  (from-interval  500))
  (def  es9  (map  (from-interval  500)  #(str % "X")))
  (def  zipped  (zip  es8  es9))
  (def  token  (subscribe  zipped  #(prn  "zipped: "  %) nil))
  ;;	"Zipped	values:	"	[0	0]
  ;;	"Zipped	values:	"	[1	2]
  (dispose  token)

  ; BELs exercise: handle exceptions through streams
  (try
    (def  es10  (take (range-es-throw 5) 5))
    (def  token  (subscribe  es10  #(prn  "es10"  %)
                                   #(prn "ex inside:" %)))
    (catch Exception e (prn "ex outside")))

    ;(deliver es10 4))

  nil)
