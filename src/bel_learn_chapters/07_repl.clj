;;
;; namespaces
;;
*ns* ; show current
(in-ns 'user) ; switch to
(in-ns 'bel-learn-chapters.07-repl) ; switch to (UNAVAILABLE?)
(clojure.core/refer-clojure) ; fix missing loaded clojure...
(ns bel-learn-chapters.07-repl) ; create

;-----

(ns bel-learn-chapters.07-repl
  (:require [clojure.pprint :as pp])
  (:import (java.io InputStream)
           (java.util ArrayList)))

(use '[clojure.repl]
     '[clojure.pprint]
     '[clojure.reflect]
     '[clojure.inspector]) ; for repl

;;
;; use in REPL
;;
(comment
  (apropos 'index)
  (find-doc "indexed")
  (doc clojure.set/index)
  (dir clojure.repl)
  (doc map)
  (source some?))

(comment
  (require '[clojure.java.javadoc :as jdoc])
  (jdoc/javadoc Math)
  (jdoc/javadoc #"a+") ; takes the type of symbol...
  ;(jdoc/javadoc java.util.regex.Pattern) ; resolves to
  (jdoc/javadoc 1/3)

  (type 4)
  (ancestors ArrayList) ; the whole tree
  (parents (type 4)) ; only direct
  (supers (type 3.4))
  (reflect 4)
  ;; as table
  (->> (reflect InputStream) :members (sort-by :name) (pp/print-table [:name :flags :parameter-types :return-type]))

  (meta some?) ; nil
  (meta 'some) ; nil
  (meta #'some?) ; prints ...
  (doc some?)
  (source some?)
  (type 3.4)
  (fn? 2))


(comment
  (clojure.pprint/pprint (macroexpand '(time (print "timing"))))
  (macroexpand '(time (print "timing")))
  (macroexpand '(->> (range 1 10)
                     (map #(* % %))
                     (reduce +))))

(comment
  (require '[clojure.inspector :as insp])
  (print-table [ {:a "x" :age 16} {:a "y" :age 56}])
  (insp/inspect-table [ {:a "x" :age 16 :table {:z 7 :v 76}} {:a "y" :age 56 :table {:z 6 :v 56}}]))

(comment
  ;(999/0)
  *e
  (pst *e)
  *1
  *2
  *3)
