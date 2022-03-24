(ns bel-learn-chapters.x-155-core-async
  (:require [clojure.core.async :as async :refer :all]))

; https://www.braveclojure.com/core-async/
; https://github.com/clojure/core.async/tree/master/examples
; https://www.youtube.com/watch?v=096pIlA3GDo

; when you use >! and <!, or parking put and parking take.
; >!! and <!! are blocking put and blocking take.

(comment
  (do
    (defn square [x] (* x x))

    (def xform
      (comp
        (clojure.core/filter even?)
        (clojure.core/filter #(= 0 (mod % 4)))
        (clojure.core/map square)
        (clojure.core/map inc)))

    (def c (async/chan 1 xform))

    (async/go
      (async/onto-chan c [5 6 8 12 15]))

    (async/go-loop [n (async/<!! c)]
      (when n
        (println n)
        (recur (async/<!! c)))))

  (do
    (def c-in (chan 10))
    (def c-out (chan 10))
    (async/pipeline 1 c-out xform c-in)
    (async/go
      (async/onto-chan! c-in (vec (range 100))))
    (async/go
      (println (<! c-out)))))


(comment

  ; https://github.com/clojure/core.async/blob/master/examples/walkthrough.clj

  ; without go / normal threads
  (let [c (chan)]
    (thread (>!! c "hello"))
    (assert (= "hello" (<!! c)))
    (close! c))

  ; with go
  (do
    (def c1 (chan))

    (go
      (>! c1 "bel")
      (close! c1))

    (go
      (let [val (<! c1)]
        (println val)))

    ; much better
    (let [c (chan)]
      (go (>! c "hello"))
      (assert (= "hello" (<!! (go (<! c)))))
      (close! c)))

  (do
    ; use the next channel for the fastest
    (let [c1 (chan)
          c2 (chan)]
      (thread (do                                           ;(Thread/sleep 100)
                (loop []
                  (let [[v ch] (alts!! [c1 c2])]
                    (println "Read" v)
                    ;(Thread/sleep 1000)
                    (when v (recur))))
                (println "thread finished")))

      (>!! c1 "hi")
      (>!! c2 "there")
      (close! c1)))                                         ; in order to get nil from alts!!

  (do
    (def c (async/chan))
    (async/put! c "foo")
    (println (async/alts!! [(async/timeout 2000) c]))
    (println (async/alts!! [(async/timeout 2000) c])))

  ;; Use `sliding-buffer` to drop oldest values when the buffer is full:
  (chan (sliding-buffer 10))

  ; thread creates chan with result
  (let [t (thread "chili")]
    (<!! t))

  ; processing Pipeline
  (let [c1 (chan)
        c2 (chan)
        c3 (chan)]
    (go (>! c2 (clojure.string/upper-case (<! c1))))
    (go (>! c3 (clojure.string/reverse (<! c2))))
    (go (println (<! c3)))
    (>!! c1 "redrum"))

  (do
    (defn upload
      [file c]
      (go (Thread/sleep (rand 100))
          (>! c file)))

    (let [c1 (chan)
          c2 (chan)
          c3 (chan)]

      (upload "serious.jpg" c1)
      (upload "fun.jpg" c2)
      (upload "sassy.jpg" c3)

      (let [[file channel] (alts!! [c1 c2 c3])]
        (println "Sending headshot notification for" file)))))

