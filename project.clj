(defproject bel-learn-lib "0.1.0-SNAPSHOT"
  :description "Bennos library with all learnings in clojure"
  :url "https://github.com/bennoloeffler/bel-learn-lib"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.taoensso/timbre "5.1.0"] ; logging, see: https://github.com/ptaoussanis/timbre
                 [lein-koan "0.1.2"]]
  :main ^:skip-aot bel-learn-lib.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
