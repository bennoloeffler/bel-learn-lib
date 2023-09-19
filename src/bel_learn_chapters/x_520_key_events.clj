(ns bel-learn-chapters.x-520-key-events
  #_(:require
      [goog.events.KeyCodes :as keycodes]
      [goog.events :as events])
  #_(:import [goog.events EventType]))


; https://tech.toryanderson.com/2020/10/22/capturing-key-presses-in-clojurescript-with-closure/
; re-pressed

(comment
  ; CLJS!
  (def l (events/listen js/window
                        EventType.KEYUP
                        (fn [key-press]
                          (println "key pressed")
                          (println "key: " (.. key-press -keyCode))
                          (println "keycodes/ENTER =" keycodes/ENTER))))
  (events/unlistenByKey l))
