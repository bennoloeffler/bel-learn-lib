(defproject bel-learn-lib "0.1.0-SNAPSHOT"
  :description "Bennos library with all learnings in clojure"
  :url "https://github.com/bennoloeffler/bel-learn-lib"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.reader "1.2.2"]
                 [com.taoensso/timbre "5.1.0"] ; x logging, see: https://github.com/ptaoussanis/timbre
                 [lein-koan "0.1.2"] ;; training exercises "medittions"
                 [quil "3.1.0"] ;; drawing to the screen
                 [com.formdev/flatlaf "0.37"] ;; swing support for big displays
                 [io.github.erdos/erdos.assert "0.2.3"] ;; power assert and examine
                 [org.clojure/tools.trace "0.7.10"] ;; tracing
                 [seesaw "1.5.0"] ;; swing for clojure
                 [ring/ring-core "1.8.2"]
                 [lein-ring "0.12.5"] ;for the lein plugin
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-devel "1.8.2"] ;; for live development
                 [proto-repl-charts "0.3.1"] ; charts in atom
                 [proto-repl "0.3.1" :exclusions [org.clojure/core.async]] ; to use datahike
                 [org.clojure/tools.namespace "1.1.0"] ; needed for protorepl
                 [clojure.java-time "0.3.2"] ; https://github.com/dm3/clojure.java-time
                 [tongue "0.2.10"]; i18n multi-lang ; 
                 [io.replikativ/datahike "0.3.3"]
                 [venantius/ultra "0.6.0"]
                 [mate-clj "1.0.0"]; included sourcecode - but does not work
                 [io.aviso/pretty "0.1.37"]            ;formatting of exceptions
                 [mvxcvi/puget "1.2.1"]                ;colour print data
                 [expound "0.8.4"]                     ;improve error messages
                 [expectations/clojure-test "1.2.1"]   ;library for testing
                 [org.clojure/tools.namespace "1.0.0"] ;reload
                 [philoskim/debux "0.6.5"]            ;dbg debugger
                 [hashp "0.1.1"]                    ;debugging #p
                 [tupelo "21.07.08"]
                 [org.clojure/data.json "2.2.1"]
                 [clj-http "3.12.3"]
                 [mount "0.1.16"]]
  :main ^:skip-aot bel-learn-lib.core
  :target-path "target/%s"
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler bel-learn-chapters.50-http/handler}
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :reveal {:dependencies [[nrepl,"0.8.3"][vlaaad/reveal "1.3.212"]]
                      :repl-options {:nrepl-middleware [vlaaad.reveal.nrepl/middleware]}}
             :dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "1.1.0"]
                                  [org.clojure/java.classpath "1.0.0"]]
                                  ;[nrepl,"0.8.3"]
                                  ;[vlaaad/reveal "1.3.212"]]
                   :repl-options {#_:nrepl-middleware #_[vlaaad.reveal.nrepl/middleware]}
                   :plugins [[com.jakemccrary/lein-test-refresh "0.24.1"]
                             [venantius/ultra "0.6.0"]]}})

