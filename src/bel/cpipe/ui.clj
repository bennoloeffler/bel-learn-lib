(ns bel.cpipe.ui
  (:require [seesaw.core :refer :all]
            [clojure.repl :refer :all])
  (:import  (com.formdev.flatlaf FlatLightLaf FlatDarkLaf)))

; http://darevay.com/talks/clojurewest2012/#/title-slide
; https://github.com/clj-commons/seesaw/wiki
; http://www.eli.sdsu.edu/courses/fall14/cs596/notes/D18SeesawGUI.pdf

(defn open [e]
  (println "open, event: " e))


(defn run-frame []
  (invoke-later
    (FlatDarkLaf/install)
    (-> (frame :title "plow"
               :size [2000 :by 1400]
               :on-close :dispose
               :menubar (menubar :items [(menu :text "file" :items [(action :name "Open..."
                                                                                   :key "menu O"
                                                                                   :handler open)
                                                                    (menu-item :text "close")])
                                         (menu :text "tool" :items [(menu-item :text "analyse")
                                                                    (menu-item :text "fold")])])
               :icon (clojure.java.io/resource "check.png"))
        show!)))

(comment
  (run-frame)

  (use 'seesaw.dev)
  (show-options (menu))
  (show-events))

"lein run -m seesaw.test.examples.launcher"