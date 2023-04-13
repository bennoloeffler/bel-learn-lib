;; # clerk notebook with RCF tests

;; ---
;; #### markdown
;; [cheat sheet md](https://www.markdownguide.org/cheat-sheet/)

;; ---
;; #### libs
(ns bel-learn-chapters.x-250-clerk-notebook
  (:require [nextjournal.clerk :as clerk]
            [hyperfiddle.rcf :refer [tests]]
            [clojure.java.io :as io])
  (:import [java.util Date]
           [javax.imageio ImageIO]
           [java.net URL]))
;; ---
;; #### start in repl
;; see: https://github.com/nextjournal/clerk#-using-clerk
(comment
  ;; start the server
  (clerk/serve! {:browse? true})
  ;; call that for update the view
  (clerk/show! 'bel-learn-chapters.x-250-clerk-notebook)
  ;; or just watch changes
  (clerk/serve! {:browse? true :watch-paths ["src"]}))

;; ---
;; #### examples
;; https://github.com/nextjournal/clerk-demo/blob/main/notebooks/introduction.clj

;; ---
;; # testing
;; #### write your code and your data
(defn square [x] (* x x))
;; #### write your tests is easy... hello 👋, try it!
(hyperfiddle.rcf/enable!)

(tests
  (square 8) := 64
  (square 9) :<> 65)

;; #### write your data
(range)

^:nextjournal.clerk/no-cache
(defonce state (atom 0))

(defn inc-counter []
  (swap! state inc))

;; I don't get the idea of caching / not-caching...
(comment
  (inc-counter))

;; # views
;; pure data - **hide code**
^{:nextjournal.clerk/visibility #{:hide}}
(def data (clerk/use-headers [["Name" "Nachname" "Alter" "m/w"]
                              ["Benno" "Löffler" 53 :m]
                              ["Sabine" "Kiefer" 51 :w]
                              ["Leo" "Kiefer" 15 :m]]))
;; wraped in table
^{:nextjournal.clerk/visibility #{:hide}}
(clerk/table  data)

(clerk/plotly {:data [{:z [[1 2 3] [3 2 1]] :type "surface"}]})

;; ---
;; html
;; ---
(clerk/html "<div><br/><br/>ach du <strong>dickes </strong> Ei...<br/><br/><div> <h4>bels heading</h4><p>lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem lorem.</p>")

(def src
  (clojure.java.io/file "src"))
(into #{} (map str) (file-seq src))

;; ---
;; cache - i don't get the idea...
;; don't update - BUT IT's UPDATED!
;; here:
(shuffle (range 5))

;; and here:
;; update after **every** change in file
^:nextjournal.clerk/no-cache
(shuffle (range 5))
;; xy
;; unicode and emojis
;; ---
{:hello "👋 world" :tacos (map (comp #(map (constantly '🌮) %) range) (range 1 100))}


;; show code
;; ---
(clerk/code (macroexpand '(when test
                            expression-1
                            expression-2)))
;; some more markdown:
;; ---
;; tick list
;;
;; - [x] Write the press release
;; - [x] Update the website
;; - [ ] Contact the media
;;
;; or show something as code in md
;; ---
;; ```
;; {
;;      "firstName": "John",
;;      "lastName": "Smith",
;;      "age": 25
;; }
;; ```

;; Or build your own colour parser and then use it to generate swatches:
(clerk/with-viewers (clerk/add-viewers
                      [{:pred #(and (string? %)
                                    (re-matches
                                       (re-pattern
                                        (str "(?i)"
                                             "(#(?:[0-9a-f]{2}){2,4}|(#[0-9a-f]{3})|"
                                             "(rgb|hsl)a?\\((-?\\d+%?[,\\s]+){2,3}\\s*[\\d\\.]+%?\\))")) %))
                        :render-fn '#(v/html [:div.inline-block.rounded-sm.shadow
                                              {:style {:width 16
                                                       :height 16
                                                       :border "1px solid rgba(0,0,0,.2)"
                                                       :background-color %}}])}])
                    ["#571845"
                     "rgb(144,12,62)"
                     "rgba(199,0,57,1.0)"
                     "hsl(11,100%,60%)"
                     "hsla(46, 97%, 48%, 1.000)"])

;;
;; ---
;; links, images
;; ---

;; - [this is a link to nowhere](https://www.example.com)
;; - pictures **do not work**
;; - here we go: ![hmmm](resources/hero.png)
;; ![hmmm](resources/hero.png)
;;
;; but this works...
(ImageIO/read (URL. "https://imgs.xkcd.com/comics/real_programmers.png"))
(ImageIO/read (URL. "https://blog.pixum.de/wp-content/uploads/2019/01/blog-frau-fotografiert-sonnenuntergang-aussichtspunkt-smartphone.jpg"))

;; local too...
(ImageIO/read (io/file "resources/hero.png"))
;; does not work: (ImageIO/read (io/file "resources/skull.svg"))
