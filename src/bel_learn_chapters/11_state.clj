(ns bel-learn-chapters.11-state)


;;
;; atom und swap! reset! update update-in assoc
;;

(def state (atom {:mouse-pos
                  {:x 0 :y 0
                   :modifiers {:ctrl false :shift false}}
                  :value 0}))
@state

;associate map key val
(assoc @state :mouse-pos {:z 12})
@state ; no change

;associate map key val - several kv-pairs
(assoc @state :mouse-pos {:z 12} :value 30)
@state ; no change

(dissoc @state :mouse-pos)
@state ; no change

;update a value by function and (many) parameters
(update @state :value + 19 1 4 6)
@state ; no change

;update a value by (complicated) function and parameters
(update @state :mouse-pos #(hash-map :x (inc (:x %1)) :y %2) 27)
@state ; no change

(update-in @state [:mouse-pos :modifiers] assoc :ctrl false :shift false :alt true)
@state ; no change

(assoc-in @state [:mouse-pos :modifiers :alt] false)
@state ; no change


;======= atomic state change
(swap! state assoc :value 999)
@state

(swap! state assoc :mouse-pos {:x 99 :z 99} :value 99)
@state

(swap! state dissoc :value)
@state

(swap! state update :mouse-pos update :x inc) ; only one!
@state

(swap! state update :mouse-pos assoc :x 9 :y 9 :z 9) ; several
@state

(swap! state update :mouse-pos :x); :x is a function on the map provided to update
@state

(swap! state dissoc :mouse-pos)
(swap! state update :mouse-pos assoc :x 1 :y 2 :z 3)
(swap! state update-in [:mouse-pos :modifiers] assoc :ctrl false :shift false)
(swap! state assoc :value 99)

(swap! state merge {:value 39 :add 29} {:another 99})

(swap! state into {:value 999}) ; transducer?

nil
