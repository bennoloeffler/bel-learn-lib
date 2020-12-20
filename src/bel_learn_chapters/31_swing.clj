(ns bel-learn-chapters.31-swing
  ;(:gen-class)
  (:require [seesaw.core :refer :all]
            [clojure.repl :refer :all])
  (:import [javax.swing JFrame JLabel JButton]
           ;[java.awt.event WindowListener]
           (com.formdev.flatlaf FlatLightLaf FlatLaf FlatDarkLaf)))



(defn swing []
  (let [frame (JFrame. "Fund manager")
        label (JLabel. "Exit on close")]
    (FlatDarkLaf/install)
    (doto frame
      (.add label)
      (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
      (.setVisible true)
      (.pack))))


(defn -main [& args]
  (swing))

(comment (-main))

;;-------------------------------------------
;; https://gist.github.com/daveray/1441520
;; http://darevay.com/talks/clojurewest2012/#/overview
;; https://github.com/eugenkiss/7guis-Clojure-Seesaw/blob/master/src/sevenguis/cells.clj

;(native!)
(FlatLightLaf/install)
(def f (frame :title "Get to know Seesaw"))
(-> f pack! show!)
;(config f :title)
;(config! f :title "BELsss win")
;(config! f :content "This is some content")
;(def lbl (label "I'm another label"))
;(config! f :content lbl)
(defn display [content]
  (config! f :content content)
  (-> f pack! show!)
  content)
;(display lbl)
;(config! lbl :background :pink :foreground "#00f")
;(config! lbl :font "ARIAL-BOLD-21")
;(def b (button :text "Click Me"))
;(display b)
;(alert "I'm an alert")
;(input "What's your favorite color?")
;(listen b :action (fn [e] (do (alert "Thanks BEL!")(prn e))))
#_(listen b :mouse-entered #(config! % :foreground :blue)
          :mouse-exited  #(config! % :foreground :red))

(def lb (listbox :model (-> 'seesaw.core ns-publics keys sort)))
(display (scrollable  lb))
(selection lb)
(def unregister-selection (listen lb :selection (fn [e] (println "Selection is " (selection e)))))
;(unregister-selection)

(def area (text :multi-line? true :font "MONOSPACED-PLAIN-30"
                :text "This
is
multi
line
text"))

;(display area)
;(text! area (java.net.URL. "https://www.spiegel.de/"))
;(display (scrollable area))
;(scroll! area :to :bottom)
;(scroll! area :to :top)
(def split (left-right-split (scrollable lb) (scrollable area) :divider-location 1/3))
(display split)
(defn doc-str [s] (-> (symbol "seesaw.core" (name s)) resolve meta :doc))
(listen lb :selection
        (fn [e]
          (when-let [s (selection e)]
            (-> area
                (text!   (doc-str s))
                (scroll! :to :top)))))


#_(def rbs (for [i [:source :doc]]
            (radio :id i :class :type :text (name i))))

#_(display (border-panel
             :north (horizontal-panel :items rbs)
             :center split
             :vgap 5 :hgap 5 :border 5))

;(select f [:JRadioButton])
;(def group (button-group))
;(config! (select f [:.type]) :group group)

#_(listen group :selection
          (fn [e]
            (when-let [s (selection group)]
              (println "Selection is " (id-of s)))))

