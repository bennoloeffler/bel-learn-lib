(ns bel-learn-lib.core
  (:gen-class))


; HOW to use the lib
; ;;;;;;;;;;;;;;;;;;
; Make a dir in the other project
; c:\projects\bel-use-learn-lib\src\bel-learn-lib
; BASICALLY: bel-learn-lib in src
; Put a symbolic link there: 
; mklink ..\..\..\..\bel-learn-lib\src\ (AS ADMIN)
; THATS IT

(defn test-the-lib
  "I just let a libs function to be called"
  []
  (println "clj-learn-lib.core/test-the-lib seems to work! Hello, World!")
  "result is comming back... lib works!")

(comment
  (+ 3 4)
  (bel-learn-lib.core/test-the-lib))

