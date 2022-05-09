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
; https://ebin.pub/download/data-oriented-programming-unlearning-objects-version-2.html

;; basic generators (local-date, simple-string)

(defn gen-local-date
  []
  (gen/fmap
    (fn [[y m d]]
      (jt/local-date y m d))
    (gen/tuple (gen/choose 1970 2050)
               (gen/choose 1 13)
               (gen/choose 1 28))))

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

(defn char-range [start end]
  (map char (range (int start) (inc (int end)))))
(def string-characters (-> [\- \_]
                           (concat (range 0 10))
                           (concat (char-range \a \z))
                           (concat (char-range \A \Z))
                           str/join))

(def simple-string-gen (gen/fmap (fn [_] (rand-str (inc (rand-int 33)) string-characters)) (s/gen int?)))

;; basic types
(s/def ::date (s/with-gen #(instance? LocalDate %) gen-local-date))
(s/def ::simple-string (s/with-gen (s/and string? #(re-matches #"[a-z0-9_-]+" %)) simple-string-gen))
(s/def ::number number?)

(s/valid? ::simple-string "abc-1/")
#_(gen/generate simple-string-gen)
#_(gen/generate (s/gen ::simple-string))
(gen/generate (s/gen ::date))


#_(defn random-local-date []
    (jt/local-date
      (+ 1970 (rand-int 50))
      (inc (rand-int 12))
      (inc (rand-int 27))))
#_(random-local-date)

#_(def simple-local-date-gen (s/gen gen-local-date))
#_(gen/sample simple-string-gen 5)


;; field types for task
(s/def ::project-name ::simple-string)
(s/def ::start-date ::date)
(s/def ::end-date ::date)
(s/def ::department ::simple-string)
(s/def ::capacity-need ::number)
(s/def ::comment ::simple-string)

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

#_(gen/generate (s/gen ::simple-string))


