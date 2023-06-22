(ns bel-learn-chapters.04-using-libs
  [:require [clojure.repl :refer :all]
            [clojure.pprint :refer :all]
            [clojure.reflect :refer :all]
            [clojure.string :as str]
            [clojure.pprint
             :refer [print-table]
             :rename {print-table pt}]] ; :verbose] ; see all loaded...
            ;[clojure.pprint :as pp]]
  [:use [clojure.repl]]
  [:import (java.io InputStream File)
           (java.util ArrayList)])

(require '[clojure.string :refer [upper-case]])
(import '(java.io File InputStream)) ; several
(import java.util.Date) ; one

;; just use java statics and constants
(comment
 (Math/sqrt 9)
 (File. "a.txt")
 (println (Date.))
 (ancestors ArrayList)
 ;; as table
 (->> (reflect InputStream) :members (sort-by :name) (pt [:name :flags :parameter-types :return-type]))
 (str/join "-" [1 3 4 "sowas" "aber" 1/4])
 Math/PI)

(comment
  ; https://github.com/borkdude/deps.add-lib
  ; PROBLEM...
  ; INCLUDE THAT in source...
  ;https://github.com/borkdude/deps.add-lib/blob/main/src/borkdude/deps/add_lib.clj
  ;(require '[borkdude.deps.add-lib :refer [add-lib]])
  ;(add-lib 'medley/medley)
  ;(require 'medley.core))
  nil)
