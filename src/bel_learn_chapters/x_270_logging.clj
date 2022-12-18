(ns bel-learn-chapters.x-270-logging
  (:require
    [clojure.string :as str]
    [taoensso.encore :as enc]
    [taoensso.timbre :as timbre
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
   :debug  :green
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
    {:output-fn      (:output-fn options)
     ; yyyy-MM-dd
     :timestamp-opts {:pattern "HH:mm:ss" :timezone (TimeZone/getTimeZone "CET")}

     :appenders      {:color-appender
                      {:enabled? true
                       :fn       (partial color-logger options)}}}))

(defn simple-output-fn [{:keys [level msg_ instant] :as data}]
  (println data)
  (str level " -> " @msg_))

(defn bels-default-output-fn
  "(fn [data]) -> final output string.
  See: timbre/default-output-fn"
  [data]
  (let [{:keys [level ?err #_vargs msg_ ?ns-str ?file hostname_
                timestamp_ ?line output-opts]}
        data]
    (str
      (when-let [ts (force timestamp_)] (str ts " "))
      (str/upper-case (name level)) " "
      (when-let [msg-fn (get output-opts :msg-fn timbre/default-output-msg-fn)]
        (msg-fn data))
      (when-let [err ?err]
        (when-let [ef (get output-opts :error-fn timbre/default-output-error-fn)]
          (when-not (get output-opts :no-stacktrace?) ; Back compatibility
            (str enc/system-newline
                 (ef data)))))
      " (" (if ?file (last (str/split ?file #"/")) "?") ":" (or ?line "?") ").")))

#_(config-timbre! {:output-fn timbre/default-output-fn :color true})
(config-timbre! {:output-fn bels-default-output-fn :color true})

;; :trace < :debug < :info < :warn < :error < :fatal < :report

;; overwrite global logging level
(set-min-level! :debug)

;; overwrite namespace logging level
(set-ns-min-level! :trace)
(defn div-zero []
  (/ 1 0))
(comment
  (div-zero) ;; see the link working in repl
  (log :trace "def")
  (debug "debug")
  (log :info "abc") ; log with timbre
  (warn "warn message text") ; log with clojure.logging
  (cl/error "clojure log")
  (log :fatal (ex-info "fatal" {}))
  (report "rep")

  ;; TUT ALLE NICHT
  (println "bel-learn-chapters.x-270-logging/div-zero (x_270_logging.clj:110).")
  (println "Syntax error compiling at (src/bel_learn_chapters/x_270_logging.clj:120:13).")
  (println "at (src/bel_learn_chapters/x_270_logging.clj:120:13)."))

