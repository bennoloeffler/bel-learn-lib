(ns bel-learn-chapters.70-date-time
  (:require [java-time :as jt]
            [tupelo.java-time :as tt]
            [java-time.repl])
  (:use [tupelo.core]
        [tupelo.test])

  (:import [java.time ZonedDateTime ZoneId]))

; https://github.com/dm3/clojure.java-time
; https://nextjournal.com/schmudde/java-time

(comment

  (tt/now->iso-str-simple)
  (tt/format->iso-str-nice (tt/now->Instant))

  ;; convert? Date to LocalDateTime
  ; https://stackoverflow.com/questions/55959630/how-can-one-get-a-java-time-datetime-using-clojure-java-time-from-inst-date-lit


  (defn inst->date-time
    "Convert a java.time.Instant to a DateTime for the supplied ZoneId"
    [inst zoneid]
    (.toLocalDateTime (ZonedDateTime/ofInstant inst zoneid)))

  (dotest
    (let [may-4    #inst "2018-05-04T01:23:45.678-00:00" ; a java.util.Date
          instant  (jt/instant may-4)]
      (spyxx may-4)
      (spyxx instant)
      (println "utc =>" (inst->date-time instant (ZoneId/of "UTC")))
      (println "nyc =>" (inst->date-time instant (ZoneId/of "America/New_York")))))

  (clojure.test/run-tests)

  ; interop
  (java.time.LocalDateTime/now)

  (jt/local-date 2015 10)
  (jt/local-time 10)
  (jt/local-date-time 2015 10)

  (jt/offset-time 10)
  (jt/offset-date-time 2015 10)
  (jt/zoned-date-time 2015 10)

  ; like Date, unix milliseconds
  (jt/instant)
  (java.util.Date.)

  (defn warm-up []
    (jt/zoned-date-time 2015 1 1)
    (jt/zoned-date-time 2015 1)
    (jt/zoned-date-time 2015))
  (warm-up)

  ; convert
  (jt/zone-id "UTC")
  (def zoned (jt/zoned-date-time))
  (def zoned-utc (jt/zoned-date-time (jt/zone-id "UTC")))
  (def inst (jt/instant))
  (def date (java.util.Date.))
  (def local (jt/local-date-time))

  (def date-zoned (jt/java-date zoned))
  (def date-zoned-utc (jt/java-date zoned-utc))
  ;; CONVERTER to make date look like local date
  ;(def date (jt/java-date local))
  (def date-local (jt/java-date local  "UTC"))

  (def date-inst (jt/java-date inst  "UTC"))
  (def zdt (jt/zoned-date-time date "UTC"))


  (jt/local-date-time)
  (-> (jt/local-date-time)
      jt/year
      jt/format)

  (jt/time-between
    (jt/local-date 1990 10 02)
    (jt/local-date 2020 10 01)
    :years))
