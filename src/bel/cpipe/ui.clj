(ns bel.cpipe.ui
  (:require [seesaw.core :refer :all]
            [seesaw.tree :refer :all]
            [clojure.repl :refer :all])
  (:import (com.formdev.flatlaf FlatLightLaf FlatDarkLaf)))

; http://darevay.com/talks/clojurewest2012/#/title-slide
; https://github.com/clj-commons/seesaw/wiki
; http://www.eli.sdsu.edu/courses/fall14/cs596/notes/D18SeesawGUI.pdf


;; TODO put cpipe in own repository...

(defn open [e]
  (println "open, event: " e))

(defn display [title create-content-fn & args]
  (invoke-later
    (FlatDarkLaf/install)                                   ; FIRST install LAF -then create content: (content)
    (let [main-frame (frame :title title :on-close :dispose)]
      (config! main-frame :content (if (seq args) (apply create-content-fn args) (create-content-fn)))
      (pack! main-frame)
      (show! main-frame)
      (println main-frame))))

(defn run-frame []
  (invoke-later
    (FlatDarkLaf/install)
    (-> (frame :title "plow"
               :size [1000 :by 700]
               :on-close :dispose
               :menubar (menubar :items [(menu :text "file" :items [(action :name "Open..."
                                                                            :key "menu O"
                                                                            :handler open)
                                                                    (menu-item :text "close")])
                                         (menu :text "tool" :items [(menu-item :text "analyse")
                                                                    (menu-item :text "fold")])])
               :icon (clojure.java.io/resource "check.png"))
        show!)))


#_(defn -main [& args]
   (display "cpipe" cpipe-draw-panel))


(comment
  (run-frame)

  (use 'seesaw.dev)
  (show-options (menu))
  (show-events (menu)))

(defn load-model []
  (simple-tree-model vector? vec [[1 2 [3]] [4]]))

(defn create-grid-panel []
  (scrollable (tree :id :tree
                    :model (load-model))))


(defn create-frame-content []
  (border-panel :north (flow-panel :items [(label "text:") (text "edit")])
                :center (scrollable (create-grid-panel))))

(defn create-frame []
  (FlatDarkLaf/setup)
  (frame :title "cpipe"
         :size [1000 :by 700]
         :on-close :dispose
         :menubar (menubar :items [(menu :text "file" :items [(action :name "Open..."
                                                                      :key "menu O"
                                                                      :handler open)
                                                              (menu-item :text "close")])
                                   #_(menu :text "tool" :items [(menu-item :text "analyse")
                                                                (menu-item :text "fold")])])
         :icon (clojure.java.io/resource "check.png")
         :content (create-frame-content)))


(defn show-frame [f]
  (invoke-later
    (-> f show!)))



(comment
  (def f (-> (frame :title "cpipe"
                    :size [1000 :by 700]
                    :on-close :dispose
                    :menubar (menubar :items [(menu :text "file" :items [(action :name "Open..."
                                                                                 :key "menu O"
                                                                                 :handler open)
                                                                         (menu-item :text "close")])
                                              #_(menu :text "tool" :items [(menu-item :text "analyse")
                                                                           (menu-item :text "fold")])])
                    :icon (clojure.java.io/resource "check.png"))))

  (.setVisible f true)
  (.pack f)
  (.dispose f))