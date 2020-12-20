(ns bel-learn-chapters.08-exception
  (:import (clojure.lang ExceptionInfo)))


(defn publish-book [book]
  (when (not (:title book))
    (throw (ex-info "book needs title" {:book book})))
  (println "publishing: " book))

(try
  (publish-book {:author "BEL" :year 2017})
  (catch ExceptionInfo e (println "publish failed: " (ex-message e) (ex-data e)))); TODO: info? how to distinguish?
