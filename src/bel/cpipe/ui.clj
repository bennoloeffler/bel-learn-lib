(ns bel.cpipe.ui
  (:require [seesaw.core :refer :all]
            [seesaw.tree :refer :all]
            [clojure.repl :refer :all]
            [seesaw.bind :as bind])
  (:use
    seesaw.graphics
    seesaw.color
    seesaw.dev)
  (:import (com.formdev.flatlaf FlatLightLaf FlatDarkLaf))
  (:import (java.awt.event KeyEvent)))

; http://darevay.com/talks/clojurewest2012/#/title-slide
; https://github.com/clj-commons/seesaw/wiki
; http://www.eli.sdsu.edu/courses/fall14/cs596/notes/D18SeesawGUI.pdf

(debug!)

(comment
  (use 'seesaw.dev)
  (show-options (menu))
  (show-events (menu)))

;; TODO put cpipe in own repository...


(def state (atom {:cursor-x 0
                  :cursor-y 0
                  :size     10}))


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

; this is a way to register subscriptions to the state
(defn state-sub [state keys f]
  (println "register keys:" keys)
  (bind/bind
    state
    (bind/notify-later)
    (bind/b-do [s]
               (let [current-value (get-in @state keys)
                     last-value (get-in @last-state keys)]
                 (when (not= current-value last-value)
                   (println keys " changed: " last-value " --> " current-value)
                   (swap! last-state assoc-in keys current-value)
                   (f current-value))))))

(defn open [e]
  (println "open, event: " e))


(defn load-model []
  (simple-tree-model vector? vec [[1 2 [3]] [4]]))



(def star
  (path []
        (move-to 0 20) (line-to 5 5)
        (line-to 20 0) (line-to 5 -5)
        (line-to 0 -20) (line-to -5 -5)
        (line-to -20 0) (line-to -5 5)
        (line-to 0 20)))

(def text-style (style :foreground (color 0 0 0)
                       :font "ARIAL-BOLD-12"))


(defn paint [c g]
  (let [;w (.getWidth c) w2 (/ w 2)
        ;h (.getHeight c) h2 (/ h 2)
        size (:size @state)
        cx (* (:cursor-x @state) size)
        cy (* (:cursor-y @state) size)]
    #_(draw g
            (ellipse 0 0 w2 h2) (style :background (color 224 224 0 128))
            (ellipse 0 h2 w2 h2) (style :background (color 0 224 224 128))
            (ellipse w2 0 w2 h2) (style :background (color 224 0 224 128))
            (ellipse w2 h2 w2 h2) (style :background (color 224 0 0 128)))
    (push g
          (rotate g 0)
          (draw g (string-shape 20 20 (str "cursor x: " (:cursor-x @state))) text-style)
          (draw g (string-shape 20 50 (str "cursor y: " (:cursor-y @state))) text-style))
    (push g
          (translate g cx cy)
          (draw g star (style :foreground java.awt.Color/BLACK :background java.awt.Color/YELLOW)))))


(defn move-up []
  ;(println "move up")
  (swap! state update :cursor-y dec))
(defn move-down []
  ;(println "move down")
  (swap! state update :cursor-y inc))
(defn move-right []
  ;(println "move right")
  (swap! state update :cursor-x inc))
(defn move-left []
  ;(println "move left")
  (swap! state update :cursor-x dec))

(def key-actions {KeyEvent/VK_UP    move-up
                  KeyEvent/VK_DOWN  move-down
                  KeyEvent/VK_RIGHT move-right
                  KeyEvent/VK_LEFT  move-left})

(defn key-listener [e]
  (let [key    (.getExtendedKeyCode e)
        action (key-actions key)]
    (if action
      (action)
      (println "key not bound: " key))))


(defn create-grid-panel []
  (let [grid-panel (canvas :id :grid-panel :paint paint)]

    (.setFocusable grid-panel true)
    (listen grid-panel #{:key-pressed} key-listener)
    (listen grid-panel #{:mouse-entered} (fn [e] (.grabFocus grid-panel)))
    (state-sub state
               [:cursor-x]
               (fn [v]
                 ;(println "val: " v)
                 (.repaint grid-panel)))
    (state-sub state
               [:cursor-y]
               (fn [v]
                 ;(println "val: " v)
                 (.repaint grid-panel)))
    grid-panel))


(defn create-tree-panel []
  (scrollable (tree :id :tree
                    :model (load-model))))


(defn create-frame-content []
  (border-panel :north (flow-panel :items [(label "text:") (text "edit")])
                :center (left-right-split
                          (scrollable (create-tree-panel))
                          (scrollable (create-grid-panel)))))

(defn create-frame []
  (FlatDarkLaf/setup)
  (let [frame (frame :title "cpipe"
                     :size [1000 :by 700]
                     :on-close :dispose
                     :menubar (menubar :items [(menu :text "file" :items [(action :name "Open..."
                                                                                  :key "menu O"
                                                                                  :handler open)
                                                                          (menu-item :text "close")])
                                               #_(menu :text "tool" :items [(menu-item :text "analyse")
                                                                            (menu-item :text "fold")])])
                     :icon (clojure.java.io/resource "check.png")
                     :content (create-frame-content))]
    (listen frame #{:key-pressed :mouse-clicked} (fn [e] (println "frame event: " e)))
    frame))



(defn show-frame [f]
  (invoke-later
    (-> f show! #_pack!)))


