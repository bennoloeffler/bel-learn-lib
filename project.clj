
; reaload and load libs
; Alembic https://spantree.net/blog/2016/05/09/useful-leiningen-plugins.html

(defproject bel-learn-lib "0.1.0-SNAPSHOT"
  :description "Bennos library with all learnings in clojure"
  :url "https://github.com/bennoloeffler/bel-learn-lib"
  :license {:name "WTFPL"
            :url  "http://www.wtfpl.net/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.taoensso/encore "3.39.0"]
                 [io.github.nextjournal/clerk "0.11.603"]
                 [org.clojure/tools.reader "1.3.6"]


                 [com.taoensso/timbre "6.0.2"] ; x logging, see: https://github.com/ptaoussanis/timbre
                 ;; route slf4j through timbre
                 [com.fzakaria/slf4j-timbre "0.3.21"]
                 ; route everything through slf4j
                 [org.slf4j/slf4j-api "1.7.14"] ;; 2.x does not work any more...
                 ;; see https://github.com/vaughnd/clojure-example-logback-integration
                 ;[ch.qos.logback/logback-classic "1.1.1"] ;;
                 [org.clojure/tools.logging "1.2.4"]
                 [org.slf4j/log4j-over-slf4j "2.0.3"]
                 [org.slf4j/jul-to-slf4j "2.0.3"]
                 [org.slf4j/jcl-over-slf4j "2.0.3"]

                 [lein-koan "0.1.5"] ;; training exercises "medittions"
                 [quil "3.1.0"] ;; drawing to the screen
                 [com.formdev/flatlaf "2.0.2"] ;; swing support for big displays
                 [io.github.erdos/erdos.assert "0.2.3"] ;; power assert and examine
                 [org.clojure/tools.trace "0.7.11"] ;; tracing
                 [seesaw "1.5.0"] ;; swing for clojure
                 [ring/ring-core "1.9.5"]
                 [lein-ring "0.12.6"] ;for the lein plugin
                 [ring/ring-jetty-adapter "1.9.5"]
                 [ring/ring-devel "1.9.5"] ;; for live development
                 [proto-repl-charts "0.3.2"] ; charts in atom
                 [proto-repl "0.3.1" :exclusions [org.clojure/core.async]] ; to use datahike
                 [org.clojure/tools.namespace "1.2.0"] ; needed for protorepl
                 [clojure.java-time "0.3.3"] ; https://github.com/dm3/clojure.java-time
                 [tongue "0.4.3"] ; i18n multi-lang ;
                 [io.replikativ/datahike "0.3.3"]
                 [venantius/ultra "0.6.0"]
                 [mate-clj "1.0.1"] ; included sourcecode - but does not work
                 [io.aviso/pretty "1.1.1"] ;formatting of exceptions
                 [mvxcvi/puget "1.3.2"] ;colour print data
                 [expound "0.9.0"] ;improve error messages
                 [expectations/clojure-test "1.2.1"] ;library for testing
                 [org.clojure/tools.namespace "1.2.0"] ;reload
                 [philoskim/debux "0.8.2"] ;dbg debugger
                 [hashp "0.2.1"] ;debugging #p
                 [org.clojure/data.json "2.4.0"]
                 [clj-http "3.12.3"]
                 [mount "0.1.16"]
                 [io.reactivex/rxclojure "1.0.0"]
                 [missionary "b.26"]
                 [com.hyperfiddle/rcf "20220405"]
                 [cprop "0.1.19"]
                 [org.clojure/tools.logging "1.2.4"]
                 [com.clojure-goes-fast/clj-async-profiler "1.0.3"]
                 [dev.weavejester/medley "1.5.0"]
                 [tupelo "22.03.09"]]


  ;:main ^:skip-aot bel-learn-lib.core
  :main ^:skip-aot user
  :target-path "target/%s"
  :plugins [[lein-ring "0.12.6"]
            [lein-cloverage "1.2.2"]]
  :ring {:handler bel-learn-chapters.50-http/handler}
  :bat-test {:parallel? true :report [:pretty {:type :junit :output-to "target/junit.xml"}]} ; :report :progress ; xunit-viewer -r . -w -p 5050 -s true

  :test-paths ["test"] ; cant get bat-test to run in "src"
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner" "--plugin" "notifier" "--watch"]
            "coverage" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner" "--plugin" "cloverage"]}
  :profiles {:uberjar  {:aot      :all
                        :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :reveal   {:dependencies [[nrepl, "0.9.0"] [vlaaad/reveal "1.3.212"]]
                        :repl-options {:nrepl-middleware [vlaaad.reveal.nrepl/middleware]}}
             :dev      {:source-paths ["dev"]
                        ;; for profiling
                        :jvm-opts ["-Djdk.attach.allowAttachSelf"]
                        :dependencies [[org.clojure/tools.namespace "1.2.0"]
                                       [org.clojure/java.classpath "1.0.0"]
                                       [nrepl, "0.9.0"]
                                       [lambdaisland/kaocha "1.67.1055"]]}
                                       ;[vlaaad/reveal "1.3.214"]]
                        ;[pjstadig/humane-test-output "0.11.0"]]

                        ;:repl-options {:nrepl-middleware [vlaaad.reveal.nrepl/middleware]}
                        ;:plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"] ; lein test-refresh
                        ;               [metosin/bat-test "0.4.4"] ; lein bat-test auto ;see eftest
                        ;               [lein-cloverage "1.2.2"]]} ; lein cloverage
             ;[venantius/ultra "0.6.0"];crashes with linux/idea2021.1]}})
             ;:injections [(require 'pjstadig.humane-test-output)
             ;(pjstadig.humane-test-output/activate!)]]}

             :bel-test {; does not work to put humane output to console runner... bat-test with 'lein with-profile bel-test  bat-test auto'
                        ;:test-paths   ["test" "src"]
                        :dependencies [;[org.clojure/tools.namespace "1.1.0"]
                                       ;[org.clojure/java.classpath "1.0.0"]
                                       ;[nrepl,"0.8.3"]
                                       ;[vlaaad/reveal "1.3.212"]
                                       [pjstadig/humane-test-output "0.11.0"]]

                        ;:repl-options {:nrepl-middleware [vlaaad.reveal.nrepl/middleware]}
                        :plugins      [;[com.jakemccrary/lein-test-refresh "0.24.1"] ; lein test-refresh
                                       [metosin/bat-test "0.4.4"]]
                        ;[venantius/ultra "0.6.0"]] ; crashes with linux/idea2021.1]}})
                        :injections   [(require 'pjstadig.humane-test-output)
                                       (pjstadig.humane-test-output/activate!)]}
             :kaocha {:dependencies [[lambdaisland/kaocha "1.67.1055"]
                                     [lambdaisland/kaocha-cloverage "1.0.75"]]}})



