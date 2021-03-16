(defproject bel-learn-lib "0.1.0-SNAPSHOT"
  :description "Bennos library with all learnings in clojure"
  :url "https://github.com/bennoloeffler/bel-learn-lib"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.reader "1.2.2"]
                 [com.taoensso/timbre "5.1.0"] ; logging, see: https://github.com/ptaoussanis/timbre
                 [lein-koan "0.1.2"] ;; training exercises "medittions"
                 [quil "3.1.0"] ;; drawing to the screen
                 [com.formdev/flatlaf "0.37"] ;; swing support for big displays
                 [erdos.assert "0.1.0"] ;; power assert and examine
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
                 [venantius/ultra "0.6.0"]]
 ; https://github.com/dm3/clojure.java-time/blob/master/README.md
                ;[mate-clj "1.0.0"]]; included sourcecode - but does not work
  :main ^:skip-aot bel-learn-lib.core
  :target-path "target/%s"
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler bel-learn-chapters.50-http/handler}
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "1.1.0"]
                                  [org.clojure/java.classpath "1.0.0"]]}})
