(ns bel.util.state-subs
  (:require [seesaw.bind :as bind]))

;; TODO history & undo

;; holds all old states in order to
;; calc real difference on updates in state-sub
(def last-state (atom {}))

;; that state may be changed from other threads
#_(future (while
            (and
              (>= (:cursor-y @state) 0)
              (<= (:cursor-y @state) 20))
            (swap! state update :cursor-x inc)
            (Thread/sleep 500)))

(defn state-sub
  "this is a way to register subscriptions to the state"
  [state keys f]
  (println "subscription - register keys:" keys)
  (bind/bind
    state
    (bind/notify-later)
    (bind/b-do [_]
               (let [current-value (get-in @state keys)
                     last-value    (get-in @last-state keys)]
                 (when (not= current-value last-value)
                   ;(println keys " changed: " last-value " --> " current-value)
                   (swap! last-state assoc-in keys current-value)
                   (f current-value))))))
