(ns bel-learn-chapters.x-270-logging
  (:require [taoensso.timbre :as timbre
             ;; Optional, just refer what you like:
             :refer [log trace debug info warn error fatal report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy set-min-level!]]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.tools.logging :refer [use-timbre]])
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



;; :trace < :debug < :info < :warn < :error < :fatal < :report
(set-min-level! :info)

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
(timbre/merge-config!
  {:appenders      {:spit (appenders/spit-appender {:fname "cr-eam.log"})}
   :timestamp-opts {:pattern "yyyy-MM-dd HH:mm:ss" :timezone (TimeZone/getTimeZone "CET")}})
;:output-fn bels-output})

; taoensso.timbre/*config*
; taoensso.timbre/default-timestamp-opts


(comment
  (log :info "abc") ; log with timbre
  (log :trace "def")) ; log with clojure.logging
