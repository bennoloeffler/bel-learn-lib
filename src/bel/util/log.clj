(ns bel.util.log
  (:require [tupelo.string :as str]
            [clojure.set :as set]))

(add-tap println) ;; could be logged somewhere else

(def logging-settings (atom {:levels  #{}
                             :max-len 0
                             :time false})) ; TODO: include time?

(defn set-levels [levels]
  (let [max-len (->> levels
                     (clojure.core/map #(str %))
                     (clojure.core/map count)
                     (apply max))]
    (swap! logging-settings assoc :levels levels :max-len max-len)))


(defn n [name]
  (when name
    (str "(name=" name ")")))


(defn l
  "Log levels expressed as one keyword or a seq of keywords.
  The active levels are given in the map 'levels'.
  Example1 (l :level1 \"this is \" value"
  [level & elems]
  (let [log-it (if (keyword? level)
                 ((:levels @logging-settings) level)
                 (seq (set/intersection (set level) (:levels @logging-settings))))]
    (when log-it
      (tap> (str (str/pad-left
                   (str/upper-case (str level))
                   (:max-len @logging-settings)
                   " ")
                 " > "
                 (str/join ", " elems))))))

(comment

  ;; switch off all log levels
  (def levels #{:no})

  ;; switch on some log levels
  (def levels #{:deliver-col :deliver :subscribe :range :read :callback :close :start})

  ;; init identation
  (set-levels levels)

  ;; log some levels
  (do (l :deliver-col "something logged, because in levels...")
      (l :no-log "something NOT logged...")
      (l :read "shorter... different aligned")
      (l :callback "the text may be \n longer the text may be longer the text may be longer the text may be longer ")
      (l :range "shorter... different aligned. Some additional elements " 14 45677)
      (l [:read :not-included :also-not] "combined levels are NOT aligned"))
  nil)

