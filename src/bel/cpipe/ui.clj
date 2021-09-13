(ns bel.cpipe.ui
  (:require [seesaw.core :refer :all]
            [clojure.repl :refer :all])
  (:import  (com.formdev.flatlaf FlatLightLaf FlatDarkLaf)))

; http://darevay.com/talks/clojurewest2012/#/title-slide
; https://github.com/clj-commons/seesaw/wiki
; http://www.eli.sdsu.edu/courses/fall14/cs596/notes/D18SeesawGUI.pdf

(defn open [e]
  (println "open, event: " e))

(defn display [title create-content-fn & args]
  (invoke-later
    (FlatDarkLaf/install) ; FIRST install LAF -then create content: (content)
    (let [main-frame (frame :title title :on-close :dispose)]
      (config! main-frame :content (if (seq args) (apply create-content-fn args)(create-content-fn)))
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


(defn -main [& args])
  ;(display "cpipe" cpipe-draw-panel))


(comment
  (run-frame)

  (use 'seesaw.dev)
  (show-options (menu))
  (show-events (menu)))

"lein run -m seesaw.test.examples.launcher"