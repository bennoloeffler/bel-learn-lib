(ns bel-learn-chapters.28-json
  (:require [clojure.data.json :as json]))

; https://github.com/clojure/data.json

(json/write-str {:a 1 :b 2})
;;=> "{\"a\":1,\"b\":2}"

(json/read-str "{\"a\":1,\"b\":2}")
;;=> {"a" 1, "b" 2}

(json/read-str "{\"a\":1,\"b\":2}"
               :key-fn keyword)

(def nested [ {:name "Benno"
               :kids {:paul 19 :benno 22 :leo 14}}
              {:name "Sabine"
               :husband "Benno"
               :age 48}])

(def json-txt (json/write-str nested))

(json/read-str json-txt
               :key-fn keyword)

