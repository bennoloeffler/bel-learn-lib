(ns bel-learn-chapters.X-120-reveal
  (:require [vlaaad.reveal :as reveal]))



; 1 install JAVA 11
; https://docs.oracle.com/en/java/javase/11/
;
; 2 Create a profile in project.clj
;   :profiles {:reveal {:dependencies [[nrepl,"0.8.3"][vlaaad/reveal "1.3.212"]]
;                       :repl-options {:nrepl-middleware [vlaaad.reveal.nrepl/middleware]}}}
;
; 3 create run configuration in cursive:
;   don't start nRepl but clojure.main
;   use profile: reveal
;
; 4 start reveal
;(require '[vlaaad.reveal :as reveal])
(comment
  (add-tap (reveal/ui)))



(tap> {:will-i-see-this-in-reveal-window? true})
(tap> {:Leo 66 :benno 14 :sabine 23 :paul 77})