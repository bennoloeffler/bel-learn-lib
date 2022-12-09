(ns bel-learn-chapters.x-270-logging
  (:require [taoensso.timbre :as timbre
             ;; Optional, just refer what you like:
             :refer [log trace debug info warn error fatal report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy set-min-level! set-ns-min-level!]]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.tools.logging :refer [use-timbre]]
            [clojure.tools.logging :as cl])
  (:import [java.util TimeZone]))

;; https://dev.to/dpsutton/logging-in-clojure-jar-tidiness-4jka

;; route everything through timbre
;[[com.taoensso/timbre "6.0.1"]

 ;; route slf4j through timbre
 ;[com.fzakaria/slf4j-timbre "0.3.21"]

 ; route everything through slf4j
 ;[org.slf4j/slf4j-api "2.0.3"]
 ;[org.clojure/tools.logging "1.2.4"]
 ;[org.slf4j/log4j-over-slf4j "2.0.3"]
 ;[org.slf4j/jul-to-slf4j "2.0.3"]
 ;[org.slf4j/jcl-over-slf4j "2.0.3"])

(use-timbre) ;Sets the root binding of `clojure.tools.logging/*logger-factory*` to use Timbre.

; format logging message
; see https://www.demystifyfp.com/clojure/marketplace-middleware/configuring-logging-using-timbre/
#_(defn- bels-output [{:keys [level msg_ instant ?file]}] ;<1>
    ; idea: use default, replace host with "" and append file.clj with line number to make it clickable
    (let [event (read-string (force msg_))] ;<2>
      (json/generate-string {:timestamp instant} ;<3>
                            :level level
                            :event event)))

; http://ptaoussanis.github.io/timbre/taoensso.timbre.html#var-*config*
#_(timbre/merge-config!
    {:appenders      {:spit (appenders/spit-appender {:fname "cr-eam.log"})}
     :timestamp-opts {:pattern "yyyy-MM-dd HH:mm:ss" :timezone (TimeZone/getTimeZone "CET")}})
;:output-fn bels-output})

; taoensso.timbre/*config*
; taoensso.timbre/default-timestamp-opts

(def ^:private color-log-map
  {:trace  :cyan
   :debug   :green
   :warn   :yellow
   :error  :red
   :fatal  :purple
   :report :blue})

(defn- color-logger [options {:keys [level output-fn] :as data}]
  (let [level-color (color-log-map level)]
    (if (and (:color options) level-color)
      (println (timbre/color-str level-color (output-fn data)))
      (println (output-fn data)))))

(defn config-timbre! [options]
  (timbre/set-config!
    {
     :timestamp-opts {:pattern "yyyy-MM-dd HH:mm:ss" :timezone (TimeZone/getTimeZone "CET")}

     :appenders {:color-appender
                 {:enabled? true
                  :fn       (partial color-logger options)}}})
  (timbre/set-level! (:level options)))



(config-timbre! {:level :info :output-fn timbre/default-output-fn :color true})

;; :trace < :debug < :info < :warn < :error < :fatal < :report

;; overwrite global logging level
(set-min-level! :debug)

;; overwrite namespace logging level
(set-ns-min-level! :warn)

(comment
  (log :trace "def")
  (debug "debug")
  (log :info "abc") ; log with timbre
  (warn "warn") ; log with clojure.logging
  (cl/error "clojure log")
  (log :fatal "fatal")
  (report "rep"))
