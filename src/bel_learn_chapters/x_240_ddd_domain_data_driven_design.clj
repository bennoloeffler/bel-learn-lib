(ns bel-learn-chapters.x-240-ddd-domain-data-driven-design
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.repl :refer [doc]]
            [clojure.string :as str]
            [java-time :as jt])
  (:import [java.time LocalDate]
           [java.util Date]))


; https://dawranliou.com/blog/domain-exploring-with-spec/
; https://gist.github.com/maacl/b0795e5f3d4ab72ca8add1e2d091e0e4
; https://github.com/didibus/clj-ddd-example
; https://day8.github.io/re-frame/data-oriented-design/


;; basic types
(s/def ::date (s/with-gen #(instance? LocalDate %) #(s/gen (LocalDate/now))))
(s/def ::simple-string (s/and string? #(re-matches #"[a-z0-9_-]+" %)))
(s/def ::number number?)

;; generator for ::simple-string
(defn char-range [start end]
  (map char (range (int start) (inc (int end)))))
(def string-characters (-> [\- \_]
                           (concat (range 0 10))
                           (concat (char-range \a \z))
                           (concat (char-range \A \Z))
                           str/join))

(defn rand-str
  "create random string with len len out of chars from string chars"
  [len chars]
  (apply str (take len (repeatedly #(rand-nth chars)))))

(defn rand-str-set
  "create a set of n random strings from
  len min-len up to max-len consisting of chars"
  [n min-len max-len chars]
  (let [min-to-max (inc ( - max-len min-len))]
   (set (take n (repeatedly #(rand-str
                               (+ min-len (rand-int min-to-max))
                               chars))))))


(defn random-local-date []
  (jt/local-date
    (+ 1970 (rand-int 50))
    (inc (rand-int 12))
    (inc (rand-int 27))))
(random-local-date)
(def simple-local-date-gen (s/gen #(random-local-date)))
(def simple-string-gen (s/gen (rand-str-set 33 1 30 string-characters)))
(gen/sample simple-string-gen 5)


;; field types for task
(s/def ::project-name (s/with-gen ::simple-string simple-string-gen))
(s/def ::start-date ::date)
(s/def ::end-date ::date)
(s/def ::department (s/with-gen ::simple-string simple-string-gen))
(s/def ::capacity-need ::number)
(s/def ::comment (s/with-gen ::simple-string simple-string-gen))

(s/def ::task (s/keys :req [::project-name ::start-date ::end-date ::department ::capacity-need]
                      :opt [::comment]))


(def task {::project-name "abc"
           ::start-date (LocalDate/now)
           ::end-date (LocalDate/now)
           ::department "xyz"
           ::capacity-need 22.0})

(s/valid? ::task task)
(s/explain ::task task)
(s/conform ::task task)
(doc ::task)

;(gen/generate (s/gen ::date))


