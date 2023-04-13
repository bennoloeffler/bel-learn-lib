(ns bel-learn-chapters.06-debugging
  [:require
    [inspect :as i]
    [clojure.string :as str]
    [clojure.pprint :refer (pprint)]
    [puget.printer :refer (cprint)]
    [erdos.assert :as pa]                                   ;power-assert
    [taoensso.timbre :refer [spy error warn info debug set-min-level!]]    ;logging
    [clojure.tools.trace :as trace :refer [dotrace trace-forms]] ;tracing
    [debux.core :refer :all]                                ; dbg
    [hashp.core :refer :all]])                              ;#p

; http://www.futurile.net/2020/05/16/clojure-observability-and-debugging-tools/
; https://github.com/jpmonettas/flow-storm-debugger

(set! *print-length* 20)

;;---------------------------------------
;; dbg und #p
;;---------------------------------------

(comment

  (use 'hashp.core)
  ;hashp
  (/ 10 #p (/ (- 12 10) (+ 10 1)))

  ;debux
  (dbg (->> (all-ns)
            (shuffle)
            (take 20)
            (map ns-name)
            sort
            (partition 4)))

  ;erdos
  (pa/examine (->> (range 10)
                   (map inc)
                   doall                                    ; because lazy result from map
                   (reduce +))))

;;---------------------------------------
;; breakpoint cursive idea / exception breakpoint / enable when other hit...
;;---------------------------------------


(defn debug-this [arg1 arg2]
  (let [from (min arg1 arg2)                                ; breakpoint may have conditions
        to (max arg1 arg2)
        start (+ arg1 arg2)]
    (->> (range from to 3)                                  ; get range
         (map #(* % 3))                                     ; multiply each by 3
         (filter odd?)                                      ; get only the odds
         (reduce #(- %1 %2) start))))                       ; reduce by minus, starting at 1000

(comment (debug-this 155 25))

;;---------------------------------------
;; TRACE and MIX #p and trace
;;---------------------------------------

(defn ^:dynamic do-div [x y]
  (/ x #p y))                                               ; setting an exception breakpoint helps

(defn ^:dynamic calc [x y]
  (let [xx (inc x) yy (dec y)]
    (do-div xx yy)))

; https://stackoverflow.com/questions/41946753/how-can-i-trace-code-execution-in-clojure
; https://github.com/clojure/tools.trace
(comment
  (dotrace [calc do-div] (calc 4 7))
  (trace-forms (+ 1 3) (* 5 6) (/ 1 1)))                    ;; To identify which form is failing

(comment (calc 10 1))                                       ; set an exception breakpoint...

(comment                                                    ; printing something in between
  (let [pow 3 value 3]
    (loop [i pow res 1]
      (if (zero? i)
        res
        (recur (dec i) (* #p res value))))))
;(recur (dec i) (* (doto res prn) value))

;;---------------------------------------
;; TRACE
;;---------------------------------------

(defn pow [value pow]
  (loop [i pow res 1]
    (if (zero? i)
      res
      (recur (dec i) (* res value)))))

(defn mult-pow [to]
  (->> (range 1N (inc to)) (map #(pow % %))))

(comment                                                    ; tracing
  (trace/trace-vars bel-learn-chapters.06-debugging/pow)
  (mult-pow 20N))

;;----------------------------------------
;; MATE-CLJ
;;---------------------------------------

(require '[mate-clj.core :as mate])
;; https://github.com/AppsFlyer/mate-clj

(comment ;; DOES NOT WORK! SHIT!
  (mate/d->> [:1 :2 :3 :4]
             shuffle
             (map str))

  ; but this does
  (dbg (->> [:1 :2 :3 :4]
            shuffle
            (map #(str % "--"))
            str/join))

  ; this too
  (def m {:body "flow test"})
  (mate/d-> m
            :body                                           ;step #1
            (clojure.string/upper-case)                     ;step #2
            (clojure.string/reverse)))                      ;step #3)

;;---------------------------------------
;; POWER ASSERTS
;;---------------------------------------

;(pa/assert (= 1 (count ( range 1 9))))
;(pa/examine (= 7 (count ( range 1 9))))
(pa/assert (= 8 (count (range 1 9))))

(comment
  (pa/examine (->> [:1 :2 :3 :4]
                   (shuffle)
                   (map #(str % "--"))
                   (doall)                                  ;see lazy ones
                   (clojure.string/join)))
  (pa/examine (->> [:1 :2 :3 :4] shuffle str/join)))

;;---------------------------------------
;; TIMBRE LOGGING
;; https://github.com/ptaoussanis/timbre
;;---------------------------------------

(comment
  (taoensso.timbre/merge-config! {:min-level :info})        ; :level debug does nothing!
  (taoensso.timbre/info "This will print")
  (taoensso.timbre/debug "error")

  ;; not thrown - but stacktrace printed
  (error (Exception. "Oh no - this is a shituation") "data 1" 1234)

  ; not thrown, not cought - but stacktrace
  (try
    (error (Exception. "Oh no - this is a shituation") "data 1" 1234)
    (catch Exception e (str "cought..." e)))

  ;; cought.. and reported
  (try
    (/ 1 0)
    (catch Exception e (str "caught exception: " (.getMessage e))))

  ;; reported but better
  (try
    (/ 1 0)
    (catch Throwable e (error e))))



(comment
  (defn my-calc [a b c] (* a b c))
  (spy (my-calc 1 2 3))
  (set-min-level! :debug)
  (->> [:1 :2 :3 :4]                                        ; spy does work
       spy
       shuffle
       spy
       (map #(str % "--"))
       vec                                                  ; doall does not work?
       spy
       str/join
       spy)

  (pa/examine
    (->> [:1 :2 :3 :4]
         (shuffle)
         (map #(str % "--"))
         (clojure.string/join)))

  (dbg (->> [:1 :2 :3 :4]
            (shuffle)
            (map #(str % "--"))
            (clojure.string/join))))

;;---------------------------------------
;; OWN MACRO
;;---------------------------------------


(defmacro dbg-bel [body]
  `(let [body# ~body]
     (println "-------- dbg-bel --------------")
     (cprint ['~body :--> body#])
     (println)
     body#))
;(macroexpand '(dbg-bel (+ 1 2)))

(comment
  (+ 5 (dbg-bel (+ 1 2)))

  (->> [:1 :2 :3 :4]
       shuffle
       dbg-bel
       (map #(str % "--"))
       dbg-bel
       str/join
       dbg-bel))

(comment (dbg-bel (my-calc 1 2 3)))


;;---------------------------------------
;; even simpler
;;---------------------------------------

(def c (atom 0))
(comment
  (->> [:1 :2 :3 :4]
       shuffle
       (#(do (println (swap! c inc) ": " %) %))
       (map str)
       (#(do (println (swap! c inc) ": " %) %))
       (map second)
       (#(do (println (swap! c inc) ": " %) %))
       (partition 2)
       (#(do (println (swap! c inc) ": " %) %))
       (map clojure.string/join)
       (#(do (println (swap! c inc) ": " %) %))))


; https://github.com/AbhinavOmprakash/snitch

(require '[snitch.core :refer [defn* defmethod* *fn *let]])


(defn* foo [e f]
       (+ e f))

;;  calling foo with random integers
(foo (rand-int 100) (rand-int 100)) ; nil

;; we can evaluate the value of a and b
[e f] ; 15  85


;; we can get the return value of foo by appending a < to foo

foo< ; see the return value
foo> ; see the calling values

(*let [x 12
       y 13
       z (* x y)]
  [x y z])

[x y z]

(defn outer []
  (defn inner [] (println "inner fun"))
  (println "outer fun"))

(dir-here)
(outer)
(inner)

; https://github.com/Ivana-/bb-inspect
(comment
  (i/inspect [:a :b {:c [1 2 3]}]))

; similar to [vlaaad/reveal "1.3.212"]
; https://github.com/djblue/portal
; https://cljdoc.org/d/djblue/portal/0.37.1/doc/editors/intellij
(comment
  (require '[portal.api :as p])
  (add-tap #'p/submit)
  (def p (p/open {:launcher :intellij}))
  (tap> :hello)
  (tap> :world)
  (tap> initial-transaction-data) ; define below first


  (def initial-transaction-data
    [

     ;;
     ;; little bit of schema,
     ;;

     {:db/ident       :movie/cast
      :db/valueType   :db.type/ref
      :db/cardinality :db.cardinality/many}

     {:db/ident       :movie/director
      :db/valueType   :db.type/ref
      :db/cardinality :db.cardinality/many}

     {:db/ident       :movie/sequel
      :db/valueType   :db.type/ref
      :db/cardinality :db.cardinality/one}

     {:db/ident     :person/name
      :db/valueType :db.type/string
      :db/unique    :db.unique/identity
      :db/index     true}

     ;;
     ;; data
     ;;

     {:person/name "James Cameron",
      :person/born #inst "1954-08-16T00:00:00.000-00:00",
      :db/id       -100}
     {:person/name "Arnold Schwarzenegger",
      :person/born #inst "1947-07-30T00:00:00.000-00:00",
      :db/id       -101}
     {:person/name "Linda Hamilton",
      :person/born #inst "1956-09-26T00:00:00.000-00:00",
      :db/id       -102}
     {:person/name "Michael Biehn",
      :person/born #inst "1956-07-31T00:00:00.000-00:00",
      :db/id       -103}
     {:person/name "Ted Kotcheff",
      :person/born #inst "1931-04-07T00:00:00.000-00:00",
      :db/id       -104}
     {:person/name "Sylvester Stallone",
      :person/born #inst "1946-07-06T00:00:00.000-00:00",
      :db/id       -105}
     {:person/name  "Richard Crenna",
      :person/born  #inst "1926-11-30T00:00:00.000-00:00",
      :person/death #inst "2003-01-17T00:00:00.000-00:00",
      :db/id        -106}
     {:person/name "Brian Dennehy",
      :person/born #inst "1938-07-09T00:00:00.000-00:00",
      :db/id       -107}
     {:person/name "John McTiernan",
      :person/born #inst "1951-01-08T00:00:00.000-00:00",
      :db/id       -108}
     {:person/name "Elpidia Carrillo",
      :person/born #inst "1961-08-16T00:00:00.000-00:00",
      :db/id       -109}
     {:person/name "Carl Weathers",
      :person/born #inst "1948-01-14T00:00:00.000-00:00",
      :db/id       -110}
     {:person/name "Richard Donner",
      :person/born #inst "1930-04-24T00:00:00.000-00:00",
      :db/id       -111}
     {:person/name "Mel Gibson",
      :person/born #inst "1956-01-03T00:00:00.000-00:00",
      :db/id       -112}
     {:person/name "Danny Glover",
      :person/born #inst "1946-07-22T00:00:00.000-00:00",
      :db/id       -113}
     {:person/name "Gary Busey",
      :person/born #inst "1944-07-29T00:00:00.000-00:00",
      :db/id       -114}
     {:person/name "Paul Verhoeven",
      :person/born #inst "1938-07-18T00:00:00.000-00:00",
      :db/id       -115}
     {:person/name "Peter Weller",
      :person/born #inst "1947-06-24T00:00:00.000-00:00",
      :db/id       -116}
     {:person/name "Nancy Allen",
      :person/born #inst "1950-06-24T00:00:00.000-00:00",
      :db/id       -117}
     {:person/name "Ronny Cox",
      :person/born #inst "1938-07-23T00:00:00.000-00:00",
      :db/id       -118}
     {:person/name "Mark L. Lester",
      :person/born #inst "1946-11-26T00:00:00.000-00:00",
      :db/id       -119}
     {:person/name "Rae Dawn Chong",
      :person/born #inst "1961-02-28T00:00:00.000-00:00",
      :db/id       -120}
     {:person/name "Alyssa Milano",
      :person/born #inst "1972-12-19T00:00:00.000-00:00",
      :db/id       -121}
     {:person/name "Bruce Willis",
      :person/born #inst "1955-03-19T00:00:00.000-00:00",
      :db/id       -122}
     {:person/name "Alan Rickman",
      :person/born #inst "1946-02-21T00:00:00.000-00:00",
      :db/id       -123}
     {:person/name  "Alexander Godunov",
      :person/born  #inst "1949-11-28T00:00:00.000-00:00",
      :person/death #inst "1995-05-18T00:00:00.000-00:00",
      :db/id        -124}
     {:person/name "Robert Patrick",
      :person/born #inst "1958-11-05T00:00:00.000-00:00",
      :db/id       -125}
     {:person/name "Edward Furlong",
      :person/born #inst "1977-08-02T00:00:00.000-00:00",
      :db/id       -126}
     {:person/name "Jonathan Mostow",
      :person/born #inst "1961-11-28T00:00:00.000-00:00",
      :db/id       -127}
     {:person/name "Nick Stahl",
      :person/born #inst "1979-12-05T00:00:00.000-00:00",
      :db/id       -128}
     {:person/name "Claire Danes",
      :person/born #inst "1979-04-12T00:00:00.000-00:00",
      :db/id       -129}
     {:person/name  "George P. Cosmatos",
      :person/born  #inst "1941-01-04T00:00:00.000-00:00",
      :person/death #inst "2005-04-19T00:00:00.000-00:00",
      :db/id        -130}
     {:person/name  "Charles Napier",
      :person/born  #inst "1936-04-12T00:00:00.000-00:00",
      :person/death #inst "2011-10-05T00:00:00.000-00:00",
      :db/id        -131}
     {:person/name "Peter MacDonald", :db/id -132}
     {:person/name  "Marc de Jonge",
      :person/born  #inst "1949-02-16T00:00:00.000-00:00",
      :person/death #inst "1996-06-06T00:00:00.000-00:00",
      :db/id        -133}
     {:person/name "Stephen Hopkins", :db/id -134}
     {:person/name "Ruben Blades",
      :person/born #inst "1948-07-16T00:00:00.000-00:00",
      :db/id       -135}
     {:person/name "Joe Pesci",
      :person/born #inst "1943-02-09T00:00:00.000-00:00",
      :db/id       -136}
     {:person/name "Ridley Scott",
      :person/born #inst "1937-11-30T00:00:00.000-00:00",
      :db/id       -137}
     {:person/name "Tom Skerritt",
      :person/born #inst "1933-08-25T00:00:00.000-00:00",
      :db/id       -138}
     {:person/name "Sigourney Weaver",
      :person/born #inst "1949-10-08T00:00:00.000-00:00",
      :db/id       -139}
     {:person/name "Veronica Cartwright",
      :person/born #inst "1949-04-20T00:00:00.000-00:00",
      :db/id       -140}
     {:person/name "Carrie Henn", :db/id -141}
     {:person/name "George Miller",
      :person/born #inst "1945-03-03T00:00:00.000-00:00",
      :db/id       -142}
     {:person/name "Steve Bisley",
      :person/born #inst "1951-12-26T00:00:00.000-00:00",
      :db/id       -143}
     {:person/name "Joanne Samuel", :db/id -144}
     {:person/name "Michael Preston",
      :person/born #inst "1938-05-14T00:00:00.000-00:00",
      :db/id       -145}
     {:person/name "Bruce Spence",
      :person/born #inst "1945-09-17T00:00:00.000-00:00",
      :db/id       -146}
     {:person/name "George Ogilvie",
      :person/born #inst "1931-03-05T00:00:00.000-00:00",
      :db/id       -147}
     {:person/name "Tina Turner",
      :person/born #inst "1939-11-26T00:00:00.000-00:00",
      :db/id       -148}
     {:person/name "Sophie Marceau",
      :person/born #inst "1966-11-17T00:00:00.000-00:00",
      :db/id       -149}
     {:movie/title    "The Terminator",
      :movie/year     1984,
      :movie/director -100,
      :movie/cast     [-101 -102 -103],
      :movie/sequel   -207,
      :db/id          -200}
     {:movie/title    "First Blood",
      :movie/year     1982,
      :movie/director -104,
      :movie/cast     [-105 -106 -107],
      :movie/sequel   -209,
      :db/id          -201}
     {:movie/title    "Predator",
      :movie/year     1987,
      :movie/director -108,
      :movie/cast     [-101 -109 -110],
      :movie/sequel   -211,
      :db/id          -202}
     {:movie/title    "Lethal Weapon",
      :movie/year     1987,
      :movie/director -111,
      :movie/cast     [-112 -113 -114],
      :movie/sequel   -212,
      :db/id          -203}
     {:movie/title    "RoboCop",
      :movie/year     1987,
      :movie/director -115,
      :movie/cast     [-116 -117 -118],
      :db/id          -204}
     {:movie/title    "Commando",
      :movie/year     1985,
      :movie/director -119,
      :movie/cast     [-101 -120 -121],
      :trivia
      "In 1986, a sequel was written with an eye to having\n  John McTiernan direct. Schwarzenegger wasn't interested in reprising\n  the role. The script was then reworked with a new central character,\n  eventually played by Bruce Willis, and became Die Hard",
      :db/id          -205}
     {:movie/title    "Die Hard",
      :movie/year     1988,
      :movie/director -108,
      :movie/cast     [-122 -123 -124],
      :db/id          -206}
     {:movie/title    "Terminator 2: Judgment Day",
      :movie/year     1991,
      :movie/director -100,
      :movie/cast     [-101 -102 -125 -126],
      :movie/sequel   -208,
      :db/id          -207}
     {:movie/title    "Terminator 3: Rise of the Machines",
      :movie/year     2003,
      :movie/director -127,
      :movie/cast     [-101 -128 -129],
      :db/id          -208}
     {:movie/title    "Rambo: First Blood Part II",
      :movie/year     1985,
      :movie/director -130,
      :movie/cast     [-105 -106 -131],
      :movie/sequel   -210,
      :db/id          -209}
     {:movie/title    "Rambo III",
      :movie/year     1988,
      :movie/director -132,
      :movie/cast     [-105 -106 -133],
      :db/id          -210}
     {:movie/title    "Predator 2",
      :movie/year     1990,
      :movie/director -134,
      :movie/cast     [-113 -114 -135],
      :db/id          -211}
     {:movie/title    "Lethal Weapon 2",
      :movie/year     1989,
      :movie/director -111,
      :movie/cast     [-112 -113 -136],
      :movie/sequel   -213,
      :db/id          -212}
     {:movie/title    "Lethal Weapon 3",
      :movie/year     1992,
      :movie/director -111,
      :movie/cast     [-112 -113 -136],
      :db/id          -213}
     {:movie/title    "Alien",
      :movie/year     1979,
      :movie/director -137,
      :movie/cast     [-138 -139 -140],
      :movie/sequel   -215,
      :db/id          -214}
     {:movie/title    "Aliens",
      :movie/year     1986,
      :movie/director -100,
      :movie/cast     [-139 -141 -103],
      :db/id          -215}
     {:movie/title    "Mad Max",
      :movie/year     1979,
      :movie/director -142,
      :movie/cast     [-112 -143 -144],
      :movie/sequel   -217,
      :db/id          -216}
     {:movie/title    "Mad Max 2",
      :movie/year     1981,
      :movie/director -142,
      :movie/cast     [-112 -145 -146],
      :movie/sequel   -218,
      :db/id          -217}
     {:movie/title    "Mad Max Beyond Thunderdome",
      :movie/year     1985,
      :movie/director [-142 -147],
      :movie/cast     [-112 -148],
      :db/id          -218}
     {:movie/title    "Braveheart",
      :movie/year     1995,
      :movie/director [-112],
      :movie/cast     [-112 -149],
                       :db/id          -219}]))