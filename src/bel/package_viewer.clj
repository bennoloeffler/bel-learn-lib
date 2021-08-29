(ns bel.package-viewer
  (:gen-class)
  (:require [seesaw.core :refer :all]
            [clojure.repl :refer :all])
  (:import (com.formdev.flatlaf FlatDarkLaf)
           (javax.swing DefaultListModel)))


(def current-package "clojure.repl")
(def current-filter "repl")
(def current-symbol-filter "")

(defn doc-str
  "the doc string of the symbol s in the current-package"
  [s] (-> (symbol current-package (name s)) resolve meta :doc))


(defn source-str
  "the source string of the symbol s in the current-package"
  [s] (try
        (-> (symbol current-package (name s)) source-fn)
        (catch Exception e (str "ERROR: " (.getMessage e)))))

(defn all-ns-symbols
  "all symbols in the current-package"
  [] (-> (symbol current-package) ns-publics keys sort))


(defn create-list-model
  "creates a list model from s"
  [s]
  (let [m (DefaultListModel.)]
    (doseq [elem s] (.addElement m elem))
    m))


(defn default-list-model-from-symbol-filter
  "creates a list model from all symbols in all packages"
  []
  (let [s (->> (all-ns)
               (reduce #(into %1 (ns-publics %2)) [])
               (reduce #(conj %1 (val %2)) [])
               (map str)
               (filter #(re-find (re-pattern current-symbol-filter) %))
               sort
               (map symbol))]
    (create-list-model s)))


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
  (let [label-ns-filter (label :text "namespace-Filter:  ")
        text-ns-filter (text :text current-filter :tip "regex to filter namespaces")
        ;button-ex (button :text "exception" :tip "throw an exception and see how it is displayed")
        label-symbol-filter (label :text "symbol-Filter:  ")
        text-symbol-filter (text :text current-symbol-filter :tip "regex to filter all symbols in all namespaces")
        ns-list (listbox :model (default-list-model-from-filter) :tip "select one namespace to see contained symbols")
        ns-symbols (listbox :model (all-ns-symbols) :tip "select one symbol to see doc and source")
        help-area (text :multi-line? true :font "MONOSPACED-PLAIN-20" :text "" :editable? false)
        source-area (text :multi-line? true :font "MONOSPACED-PLAIN-20" :text "" :editable? false)
        help-source-split (top-bottom-split (scrollable help-area) (scrollable source-area) :divider-location 1/2)
        split (left-right-split (scrollable ns-symbols) (scrollable help-source-split) :divider-location 1/3)
        printer (text :text "no status message yet..." :editable? false)
        p (border-panel :west (scrollable ns-list)
           :north (flow-panel :align :left :hgap 40 :items [label-ns-filter text-ns-filter #_button-ex label-symbol-filter text-symbol-filter]) :center split :south printer)]
    (config! frame :content p)

    #_(listen button-ex :action-performed
        (fn [_]
          (/ 1 0)))

    (listen ns-symbols :selection
      (fn [e]
        (let [s (selection e)
              contains (.contains (str s) "/")
              s (if contains
                  (do
                    (def current-package (subs (str (namespace s)) 2))
                    (name s))
                  s)]
          (when s
            (-> help-area
                (text!   (doc-str s))
                (scroll! :to :top))
           (-> source-area
               (text!   (source-str s))
               (scroll! :to :top))
           (text! printer (str "SYMBOL:  " s "     in NAMESPACE  " current-package))))))

    (listen text-ns-filter :key-released
      (fn [e]
        (try
          (let [_ (def current-filter (text text-ns-filter))
                m (default-list-model-from-filter)]
            (text! printer (str "found:  " (.getSize m) "   ( " (text e) " )"))
            (set-model* ns-list m))
          (catch Exception e (text! printer (str "REGEX ERROR: " (.getMessage e)))))))

    (listen text-symbol-filter :key-released
      (fn [e]
        (try
          (let [_ (def current-symbol-filter (text text-symbol-filter))
                m (default-list-model-from-symbol-filter)]
            (text! printer (str "found:  " (.getSize m) "   ( " (text e) " )"))
            (set-model* ns-symbols m))
          (catch Exception e (text! printer (str "REGEX ERROR: " (.getMessage e)))))))

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
  (invoke-later
    (set-frame-content! f)
    (-> f  show!)))
    ;(toggle-full-screen! f))


(defn dispose-frame [f]
  (dispose! f))


(defn -main [& _]
  (let [f (create-frame)]
    (-> f show-frame)
    f))



(comment
  (def local-frame (-main))
  (dispose-frame local-frame)
  (+ 5 7 (- 10 3)))



