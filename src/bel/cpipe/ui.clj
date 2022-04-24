(ns bel.cpipe.ui
  (:require [seesaw.core :refer :all]
            [seesaw.tree :refer :all]
            [clojure.repl :refer :all]
            [bel.util.state-subs :refer [state-sub]]
            [mount.core :as m :refer [defstate]])

  (:use
    seesaw.graphics
    seesaw.color
    seesaw.dev)
  (:import (com.formdev.flatlaf FlatLightLaf FlatDarkLaf))
  (:import (java.awt.event KeyEvent)
           (java.awt Color Rectangle)))

; http://darevay.com/talks/clojurewest2012/#/title-slide
; https://github.com/clj-commons/seesaw/wiki
; http://www.eli.sdsu.edu/courses/fall14/cs596/notes/D18SeesawGUI.pdf

(debug!)

(comment
  (use 'seesaw.dev)
  (show-options (menu))
  (show-events (menu)))

;; TODO put cpipe in own repository...


#_(def state (atom {:cursor-x  0
                    :cursor-y  0
                    :zoom      1.0
                    :grid-size 10
                    :size-x    3000
                    :size-y    1000}))

(declare create-frame)

(defn create-ui
  "create the map of data used for ui"
  []
  (println "create-ui")
  (let [ui-state (atom {:cursor-x  0
                        :cursor-y  0
                        :zoom      1.0
                        :grid-size 10
                        :size-x    3000
                        :size-y    1000})
        frame (create-frame ui-state)]
    {:ui-state ui-state
     :frame frame}))

(defn dispose-ui [state]
  (println "dispose-ui")
  (.dispose (:frame state)))


(defstate ui
          :start (create-ui)
          :stop (dispose-ui ui))

(defn state
  "access state like (:cursor-x @state)"
  []
  (:ui-state ui))

(defn open [e]
  (println "open, event: " e))


(defn load-model []
  (simple-tree-model vector? vec [[1 2 [3]] [4]]))

#_(def star
    (path []
          (move-to 0 0) (line-to 20 0)
          (line-to 20 20) (line-to 0 20)))

(defn grid [g w h size]
  (doseq [x (range 0 w size)]
    (draw g (line x 0 x h) (style :foreground Color/DARK_GRAY)))
  (doseq [y (range 0 h size)]
    (draw g (line 0 y w y) (style :foreground Color/DARK_GRAY))))


(def text-style (style :foreground (color 0 0 0)
                       :font "ARIAL-BOLD-12"))


(defn grid-to-pixel [x y])

(defn paint [c g]
  (let [grid-size (:grid-size @state)
        cx        (* (:cursor-x @state) grid-size)
        cy        (* (:cursor-y @state) grid-size)
        z         (:zoom @state)
        w         (.getWidth c)
        h         (.getHeight c)
        w-zoomed  (/ w z)
        h-zoomed  (/ h z)]

    #_(draw g
            (ellipse 0 0 w2 h2) (style :background (color 224 224 0 128))
            (ellipse 0 h2 w2 h2) (style :background (color 0 224 224 128))
            (ellipse w2 0 w2 h2) (style :background (color 224 0 224 128))
            (ellipse w2 h2 w2 h2) (style :background (color 224 0 0 128)))
    (push g
          (scale g z)
          ;(grid g w-zoomed h-zoomed grid-size)
          (draw g (rect cx cy grid-size) (style :foreground Color/YELLOW))
          (draw g (string-shape 20 20 (str "cursor x: " (:cursor-x @state))) text-style)
          (draw g (string-shape 20 50 (str "cursor y: " (:cursor-y @state))) text-style)
          (draw g (string-shape 20 80 (str "zoom: " (:zoom @state))) text-style)
          (draw g (string-shape 20 110 (str "width: " w)) text-style)
          (draw g (string-shape 20 140 (str "height: " h)) text-style))))
;(translate g cx cy))))

#_(push g
        (rotate g 0)
        (draw g (string-shape 20 20 (str "cursor x: " (:cursor-x @state))) text-style)
        (draw g (string-shape 20 50 (str "cursor y: " (:cursor-y @state))) text-style)
        (draw g (string-shape 20 80 (str "zoom: " (:zoom @state))) text-style)
        (draw g (string-shape 20 110 (str "width: " w)) text-style)
        (draw g (string-shape 20 140 (str "height: " h)) text-style))
;(draw g star (style :foreground java.awt.Color/BLACK :background java.awt.Color/YELLOW)))))



(defn move-up []
  ;(println "move up")
  (when (> (:cursor-y @state) 0)
    (swap! state update :cursor-y dec)))

(defn move-down []
  ;(println "move down")
  (swap! state update :cursor-y inc))

(defn move-right []
  ;(println "move right")
  (swap! state update :cursor-x inc))

(defn move-left []
  ;(println "move left")
  (when (> (:cursor-x @state) 0)
    (swap! state update :cursor-x dec)))

(defn zoom-in []
  ;(println "move left")
  (swap! state update :zoom #(* % 1.1)))

(defn zoom-out []
  ;(println "move left")
  (swap! state update :zoom #(* % 0.9)))

(def key-actions {KeyEvent/VK_UP    move-up
                  KeyEvent/VK_DOWN  move-down
                  KeyEvent/VK_RIGHT move-right
                  KeyEvent/VK_LEFT  move-left
                  KeyEvent/VK_PLUS  zoom-in
                  KeyEvent/VK_MINUS zoom-out})

(defn key-listener [e]
  (let [key    (.getExtendedKeyCode e)
        action (key-actions key)]
    (if action
      (action)
      (println "key not bound: " key))))

(defn scroll-cursor-to-visible [component]
  (let [vr   (.getVisibleRect component)
        sp   (.getParent component)
        ;_    (println (type sp))
        g    (@state :grid-size)
        ;area (* 1 g)
        ; scroll panel?
        w2   (/ (.width vr) 2)
        h2   (/ (.height vr) 2)
        ;_    (println "w/2 " w2 " h/2 " h2)
        x    (* (@state :cursor-x) g)
        y    (* (@state :cursor-y) g)
        ;r (Rectangle. x y g g)
        r    (Rectangle. (- x w2) (- y h2) (* 2 w2) (* 2 h2))]
        ;_ (println "scroll to: " r)]
    (.scrollRectToVisible component r)))



(defn update-grid-panel [grid-panel]
  (scroll-cursor-to-visible grid-panel)
  (.revalidate grid-panel)
  (.repaint grid-panel))


(defn create-grid-panel [state]
  (let [size-x     (:size-x @state)
        size-y     (:size-y @state)
        grid-panel (canvas :id :grid-panel :paint paint :size [size-x :by size-y])]

    (.setFocusable grid-panel true)
    (listen grid-panel #{:key-pressed} key-listener)
    (listen grid-panel #{:mouse-entered} (fn [e] (.grabFocus grid-panel)))
    (state-sub state
               [:cursor-x]
               (fn [v]
                 ;(println "val: " v)
                 (update-grid-panel grid-panel)))
    (state-sub state
               [:cursor-y]
               (fn [v]
                 ;(println "val: " v)
                 (update-grid-panel grid-panel)))
    ;(.revalidate grid-panel)))
    (state-sub state
               [:zoom]
               (fn [zoom]
                 (let [;size (config grid-panel :size)
                       ;w    (.width size)
                       ;h    (.height size)
                       nw (int (* (:size-x @state) zoom))
                       nh (int (* (:size-y @state) zoom))]
                   ;(when (> w 0)
                   ;(println " nw: " nw " nh: " nh)
                   (config! grid-panel :size [nw :by nh])
                   (update-grid-panel grid-panel))))
                     ;(.revalidate grid-panel))))
    grid-panel))


(defn create-tree-panel []
  (scrollable (tree :id :tree
                    :model (load-model))))


(defn create-frame-content [ui-state]
  (border-panel :north (flow-panel :items [(label "text:") (text "edit")])
                :center (left-right-split
                          (scrollable (create-tree-panel))
                          (scrollable (create-grid-panel ui-state)))))

(defn create-frame [ui-state]
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
                     :content (create-frame-content ui-state))]
    (listen frame #{:key-pressed :mouse-clicked} (fn [e] (println "frame event: " e)))
    frame))


(defn show-frame [f]
  (invoke-later
    (-> f show! #_pack!)))


