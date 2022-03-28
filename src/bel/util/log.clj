(ns bel.util.log
  (:require [tupelo.string :as str]))

(add-tap println)
(def levels #{:no}) ;#{:deliver-col :deliver :subscribe :range :read :callback :close :start})
(def max-len (->> levels
                  (clojure.core/map #(str %))
                  (clojure.core/map count)
                  (apply max)))
(defn n [name]
  (if name
    (str "(name=" name ")")))

(defn l
  "Log levels expressed as one keyword or a seq of keywords.
  The active levels are given in the map 'levels'.
  Example1 (l :level1 \"this is \" value"
  [level & elems]
  (let [log-it (if (keyword? level)
                 (levels level)
                 (seq (clojure.set/intersection (set level) levels)))]
    (if log-it
      (tap> (str (str/pad-right
                   (str/upper-case (str level))
                   max-len
                   "-")
                 "> "
                 (str/join elems))))))
