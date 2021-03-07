(ns bel-learn-chapters.80-i18n
  (:require [tongue.core :as i18n])) ;https://github.com/tonsky/tongue

(def dicts
  { :en { ;; simple keys
          :color "Color"
          :flower "Flower"

          ;; namespaced keys
          :weather/rain   "Rain"
          :weather/clouds "Clouds"

          ;; nested maps will be unpacked into namespaced keys
          ;; this is purely for ease of dictionary writing
          :animals { :dog "Dog"   ;; => :animals/dog
                     :cat "Cat"} ;; => :animals/cat

          ;; substitutions
          :welcome "Hello, {1}!"
          :between "Value must be between {1} and {2}"
          ;; For using a map
          :mail-title "{user}, {title} - Message received."

          ;; arbitrary functions
          :count (fn [x]
                   (cond
                     (zero? x) "No items"
                     (= 1 x)   "1 item"
                     :else     "{1} items"))} ;; you can return string with substitutions


    :en-GB { :color "colour"} ;; sublang overrides
    :tongue/fallback :en})     ;; fallback locale key


(def translate ;; [locale key & args] => string
  (i18n/build-translate dicts))

(translate :en :color) ;; => "Color"

;; namespaced keys
(translate :en :animals/dog) ;; => "Dog", taken from { :en { :animals { :dog "Dog }}}

;; substitutions
(translate :en :welcome "Nikita") ;; => "Hello, Nikita!"
(translate :en :between 0 100) ;; => "Value must be between 0 and 100"
(translate :en :mail-title {:user "Tom" :title "New message"}) ;; => "Tom, New message - Message received."

;; if key resolves to fn, it will be called with provided arguments
(translate :en :count 0) ;; => "No items"
(translate :en :count 1) ;; => "1 item"
(translate :en :count 2) ;; => "2 items"

;; multi-tag locales will fall back to more generic versions
;; :zh-Hans-CN will look in :zh-Hans-CN first, then :zh-Hans, then :zh, then fallback locale
(translate :en-GB :color) ;; => "Colour", taken from :en-GB
(translate :en-GB :flower) ;; => "Flower", taken from :en

;; if there’s no locale or no key in locale, fallback locale is used
(translate :ru :color) ;; => "Color", taken from :en as a fallback locale

;; if nothing can be found at all
(translate :en :unknown)
