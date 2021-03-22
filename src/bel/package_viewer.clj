(ns bel.package-viewer
  (:gen-class)
  (:require [seesaw.core :refer :all]
            [clojure.repl :refer :all])
  (:import  (com.formdev.flatlaf FlatLightLaf FlatDarkLaf)))


(def current-package "clojure.repl")
(def current-filter "repl")


(defn doc-str
  "the doc string of the symbol s in the current-package"
  [s] (-> (symbol current-package (name s)) resolve meta :doc))


(defn source-str
  "the source string of the symbol s in the current-package"
  [s] (-> (symbol current-package (name s)) source-fn))


(defn all-ns-symbols
  "all symbols in the current-package"
  [] (-> (symbol current-package) ns-publics keys sort))


(defn create-list-model [s]
  "creates a list model from s"
  (let [m (javax.swing.DefaultListModel.)]
    (doseq [elem s] (.addElement m elem))
    m))


(defn default-list-model-from-ns
  "creates a list model from all-ns-symbols in currrent-package"
  [] (create-list-model (all-ns-symbols)))


(defn default-list-model-from-filter
  "creates a list model from all-ns with current-filter"
  []
  (let [s (->> (all-ns)
               (map ns-name)
               (map str)
               (filter #(re-find (re-pattern current-filter) %))
               sort)]
    (create-list-model s)))


(defn set-frame-content!
  "fill the frame with components - create the ui"
  [frame]
  (let [label-filter (label :text "Filter:  ")
        filter-ns (text :text current-filter :tip "regex to filter namespaces")
        button-ex (button :text "exception" :tip "throw an exception and see how it is displayed")
        ns-list (listbox :model (default-list-model-from-filter) :tip "select one namespace to see contained symbols")
        ns-symbols (listbox :model (all-ns-symbols) :tip "select one symbol to see doc and source")
        help-area (text :multi-line? true :font "MONOSPACED-PLAIN-40" :text "" :editable? false)
        source-area (text :multi-line? true :font "MONOSPACED-PLAIN-40" :text "" :editable? false)
        help-source-split (top-bottom-split (scrollable help-area) (scrollable source-area) :divider-location 1/2)
        split (left-right-split (scrollable ns-symbols) (scrollable help-source-split) :divider-location 1/3)
        printer (text :text "no status message yet..." :editable? false)
        p (border-panel :west (scrollable ns-list) :north (flow-panel :align :left :hgap 20 :items [label-filter filter-ns button-ex]) :center split :south printer)]
    (config! frame :content p)

    (listen button-ex :action-performed
      (fn [_]
        (/ 1 0)))

    (listen ns-symbols :selection
       (fn [e]
         (when-let [s (selection e)]
           (-> help-area
               (text!   (doc-str s))
               (scroll! :to :top))
           (-> source-area
               (text!   (source-str s))
               (scroll! :to :top))
           (text! printer (str "selected symbol:  " s)))))

    (listen filter-ns :key-released
     (fn [e]
      (let [_ (def current-filter (text filter-ns))
            m (default-list-model-from-filter)]
       (text! printer (str "found:  " (.getSize m) "   ( " (text e) " )"))
       (set-model* ns-list m))))

    (listen ns-list :selection
     (fn [e]
         (when-let [s (selection e)]
           (text! printer (str "try switching to name-space:  " s))
           (def current-package (str  s))
           (set-model* ns-symbols (default-list-model-from-ns)))))))


(defn create-frame []
  (FlatDarkLaf/install)
  (frame :title "BELs namespace-viewer" :size [2000 :by 1400]))


(defn show-frame [f]
  (set-frame-content! f)
  (-> f  show!))
  ;(toggle-full-screen! f))


(defn dispose-frame [f]
  (dispose! f))


(defn -main [& args]
 (-> (create-frame) show-frame))


(comment
  (-main))