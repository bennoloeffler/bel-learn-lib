(ns bel-learn-chapters.x-169-rx-transd-core-async-test
  (:require [clojure.test :refer :all]
            [bel.util.log :refer :all]
            [bel-learn-chapters.x-169-rx-transd-core-async :as tes :refer :all]
            [clojure.core.async :refer [thread timeout pipeline go-loop <! >! <!! >!! close! chan mult tap go]]
            [clojure.tools.trace :refer :all]
            [debux.core :refer :all]))


(deftest to-test-test)


(defn helper-collect-callbacks
  "receives or creates an event stream, subsribes to it
  and collects all values in a collection. As long, as there
  are less than n-expect. But times out after 3 sec.
  Returns [es subscription vector-received] via channel.
  "
  ([n-expect] (helper-collect-callbacks (event-stream) n-expect))
  ([es n-expect]
   (thread (let [started      (System/currentTimeMillis)
                 vec-received (atom [])
                 ; performance problem comes from the collisions here?
                 callback     #(swap! vec-received conj %)
                 sub          (subscribe es callback)]
             (while (and
                      (< (count @vec-received) n-expect)
                      (< (- (System/currentTimeMillis) started) 3000))
               (<!! (timeout 5)))

             [es sub vec-received]))))


(deftest event-stream-test
  (testing "The simplest case: one event-stream. One Event."
    (let [es     (event-stream)
          res-ch (helper-collect-callbacks es 1)]
      (Thread/sleep 100)

      (deliver es :something)
      (Thread/sleep 100)
      ; need some time to deliver
      (let [[es _ received] (<!! res-ch)]
        (Thread/sleep 10)
        (is (= (count @received) 1))
        (is (= (first @received) :something))))))


(deftest deliver-test-many
  (testing "1000 events to the callback."
    (let [num-of-events 1000
          es            (event-stream)
          res-ch        (helper-collect-callbacks es num-of-events)]
      (Thread/sleep 100)
      ;(is (= (count @received) 0))
      (doseq [v (range num-of-events)]
        (deliver es v))
      (Thread/sleep 100)
      ; 5 ms is not enough - 1000 go blocks inside deliver take a while
      (let [[es _ received] (<!! res-ch)]
        (is (= num-of-events (count @received)))))))


(deftest deliver-before-subscribe-unbuffered
  (testing "unbuffered - many events delivered before the subsription comes..."
    (let [num-of-events 10
          es            (event-stream (chan) "es-do-not-start" true)
          _             (go (doseq [v (range num-of-events)]
                              (<! (timeout 5)) ; some will come through
                              (deliver es v)))
          res-ch        (helper-collect-callbacks es num-of-events)]
      (start-subscriptions es)
      (Thread/sleep 150)
      (deliver es ::tes/complete)
      (Thread/sleep 150)
      (let [[es _ received] (<!! res-ch)]
        (is (= num-of-events (count @received)))))))


(deftest deliver-col-subscribe-buffered
  (testing "many events delivered in batch to 'empty' subsription"
    (let [num-of-events 100
          es            (event-stream (chan num-of-events) "es-do-not-start" true)
          _             (thread
                          (Thread/sleep 20)
                          (attach-to-col es (range num-of-events)))
          res-ch        (helper-collect-callbacks es num-of-events)]
      (start-subscriptions es)
      (Thread/sleep 150)
      (deliver es ::tes/complete)
      (Thread/sleep 150)

      (let [[es _ received] (<!! res-ch)]
        ;(println received)
        (is (= num-of-events (count @received)))))))

(deftest deliver-col-subscribe-empty-buffered
  (testing "many events delivered in batch to 'empty' subsription"
    (let [num-of-events 100
          es            (event-stream (chan))
          _             (thread
                          (Thread/sleep 50)
                          (attach-to-col es (range num-of-events)))
          i             (atom 0)]
      (subscribe es (fn [_] (swap! i inc)))
      (Thread/sleep 100)
      (is (= num-of-events @i)))))

(deftest dispose-and-close-test
  (testing "closing"
    (let [es   (event-stream (chan 10) "name" false)
          i    (atom 0)
          j    (atom 0)
          sub1 (subscribe es (fn [_] (swap! i inc)))
          sub2 (subscribe es (fn [_] (swap! j inc)))
          _    (attach-to-col es (range 3))]

      (Thread/sleep 150)
      (start-subscriptions es)

      ; this makes sub1 stop listening
      (dispose sub1)
      (Thread/sleep 30)

      ; disposing did not stop the event channnel from working
      (is (not (completed? es)))

      ; those will not be delivered to sub1 but to sub2 because sub1 was disposed
      (attach-to-col es (range 3))
      (Thread/sleep 100)
      (is (= 3 @i))
      (is (= 6 @j))

      ; now deliver the killing signal for event channel namespaced ::complete
      (deliver es ::tes/complete)
      (Thread/sleep 30)
      (is (completed? es))

      ; has no effekt any more
      (deliver es 7)
      (Thread/sleep 100)
      (is (= 3 @i))
      (is (= 6 @j)))))

(deftest transducer-test
  (let [xform          (comp ;(map inc)
                         (map #(/ % 3))
                         (filter #(> % 2)))
        es-source      (event-stream (chan))
        es-transformed (-> es-source
                           (trans xform))
        res-ch         (helper-collect-callbacks es-transformed 1)]

    (is (= "trans-es" (.name es-transformed)))
    (attach-to-col es-source [1 9 81])
    (Thread/sleep 100)
    (deliver es-source ::tes/complete)
    (Thread/sleep 100)
    ;(dispose sub)
    (let [[es _ received] (<!! res-ch)]
      (is (= [3 27] @received)))))

(deftest atom-subs-test
  (let [a         (atom 1)
        es-source (attach-to-atom (event-stream (chan 2) "my-chan" true) a)
        res-ch    (helper-collect-callbacks es-source 2)]
    (Thread/sleep 50)
    (swap! a inc) ; 2
    (Thread/sleep 50)
    (swap! a inc) ; 3
    (Thread/sleep 50)
    (start-subscriptions es-source) ; here we start listening!
    (swap! a inc)
    (Thread/sleep 35) ; SOME TIME NEEDED TO KEEP RIGHT SEQUENCE!
    (swap! a inc)
    (Thread/sleep 35) ; SOME TIME NEEDED TO KEEP RIGHT SEQUENCE!
    (swap! a inc)
    (Thread/sleep 50) ; SOME TIME NEEDED TO KEEP RIGHT SEQUENCE!
    ;(deliver es-source ::tes/complete)
    (let [[_ _ received] (<!! res-ch)]
      (is (= [4 5 6] @received)))))

(deftest intervall-subs-test
  (let [val       (atom 10)
        es-source (attach-to-interval (event-stream (chan 2) "my-chan" true) 50 #(do (swap! val inc) @val))
        res-ch    (helper-collect-callbacks es-source 3)]
    (Thread/sleep 250)
    (start-subscriptions es-source) ; here we start listening!
    (Thread/sleep 250)
    ;(deliver es-source ::tes/complete)
    (let [[es subscription received] (<!! res-ch)]
      (dispose subscription)
      (deliver es ::tes/complete)
      (is (= [11 12 13 14 15] @received)))))





(deftest timing-start-problem
  (let [in-ch   (chan)
        mult-ch (mult in-ch)
        out-ch  (chan)]

    (go
      (doseq [v (range 10)]
        (>! in-ch v)))

    ; THIS IS THE TIMING PROBLEM (mult in-ch) DROPS
    ; 1 there is a mult-ch between in-ch and out-ch (for good reason: more than one subscriber)
    ; 2 there are values delivered to in-ch
    ; 3 then there is some waiting time e.g due to whatever
    ; 4 during this time, when the mult-ch is already connected to in-ch BUT NOT YET to out...
    ; 5 it drops all 10 values - see doc of mult
    ; 6 when the tap connects mult-ch with out-ch, the values have already been dropped and are lost

    ; 1 ms - all values come through
    ; 5 ms - all values are dropped to nirvana
    (Thread/sleep 5)

    (tap mult-ch out-ch)

    (go-loop []
      (let [v (<! out-ch)]
        (if v
          (do
            ;(println v)
            (recur))
          (println "closed"))))))


(deftest ->EventStream-test)

(deftest behavior-test
  (let [b      (behavior (System/currentTimeMillis))
        es     (sample b 20)
        res-ch (helper-collect-callbacks es 4)]
    (Thread/sleep 100)
    (deliver es ::tes/complete)
    (let [[es subscription received] (<!! res-ch)]
      (dispose subscription)
     (is (<= 4 (count @received))))))

(comment
  (def time-behavior (behavior (System/nanoTime)))
  @time-behavior
  (def time-stream (sample time-behavior 150))
  (def tok (subscribe time-stream #(prn "t=" %)))
  (Thread/sleep 1000)
  (dispose tok)
  ; could be done with ::tes/complete
  (complete time-stream))

;;
;; TODO: test flatmap and implement split, zip ???
;;


