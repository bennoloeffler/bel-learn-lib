(ns bel-learn-chapters.60-datahike-ex-data
  (:require [datahike.api :as d]
            [clojure.inspector :as insp])
  (:import  (com.formdev.flatlaf FlatLightLaf FlatLaf FlatDarkLaf)))

; https://nextjournal.com/try/learn-xtdb-datalog-today/learn-xtdb-datalog-today


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
    :db/id          -219}])

(comment

  (set! *print-length* 100)

  ;; define configuration, either
  ;; MEM or
  (def cfg {:store {:backend :mem
                    :id "bels-db"}
            :schema-flexibility :read
            :initial-tx initial-transaction-data})
  ;; FILE
  (def cfg {:name               "bels-db"
            :store              {:backend :file
                                 :path "/tmp/example"}
            :schema-flexibility :read
            :initial-tx initial-transaction-data}) ; in order to get relations right



  ;; create the in-memory database
  (d/create-database cfg)

  ;; connect to it
  (def conn (d/connect cfg))

  ;; cleanup previous database
  (d/delete-database cfg)


  ;; find all db/ids of all movies
  ;; = all EAV, that start have a attribute of :movie/title
  ;; what is the quote for? '[:find ...
  ;; Just to prevent SOMETHING inside the query string to be interpreted, e.g. ?e
  (d/q '[:find ?e
         :where [?e :movie/title]]
       @conn)

  ;; try this without quote
  (def the-query '[:find ?e
                   :where [?e :movie/title]])

  ;; return entity ids flat...
  (d/q '[:find [?e ...]
         :where
         [?e :movie/title]]
       @conn)

  ;; create a new movie
  (d/transact conn [{:movie/title "Silvester Singing"}])

  ;; return entity id (db/id) and title and year.
  ;; Movies, that have no year entry are not found!
  ;; Silvester Singing is NOT part of the results.
  (d/q '[:find ?e ?t ?y
         :where
         [?e :movie/title ?t]
         [?e :movie/year ?y]]
       @conn)

  ;; Silvester Singing is part of the results
  (d/q '[:find ?e ?t
         :where
         [?e :movie/title ?t]]
       @conn)

  ;; Silvester Singing is part of the results
  (def silverster-movie-id (first (d/q '[:find [?e ...]
                                         :where
                                         [?e :movie/title "Silvester Singing"]]
                                       @conn)))

  ;; change the title
  (d/transact conn [{:db/id       silverster-movie-id
                     :movie/title "Silvester Singing 2022/23"}])

  ;; add a year to the entity
  (d/transact conn [{:db/id      silverster-movie-id
                     :movie/year 2022}])

  ;; query in vector format
  (def all-titles-only-vec '[:find ?title
                             :where
                             [_ :movie/title ?title]])
  ;; just all movie titles
  (d/q all-titles-only-vec @conn)

  ;; query in map format do work differently in datahike
  (def all-titles-only-map '{:find [?title]
                             :where
                             ; _ is a placeholder for everything
                             [[_ :movie/title ?title]]})
  ;; some more map keys...
  (d/q {:query all-titles-only-map
        :args  [@conn]})

  ;; find all :db/id (entity ids) of persons with name
  (d/q '[:find ?e
         :where [?e :person/name _]]
       @conn)
  (d/q '[:find ?e
         :where [?e :person/name]] ;; identical - just leave the trailing joker out
       @conn)

  ;; find all movie names of 1987
  (d/q '[:find [?title ...]
         :where
         [?e :movie/title ?title]
         [?e :movie/year 1987]]
       @conn)
  (d/q '[:find [?title ...]
         :where ;; seqence does not influence result - but performance!
         [?e :movie/year 1987]
         [?e :movie/title ?title]]
       @conn)

  ;; the cast of lethal waepon
  (d/q '[:find ?cast ?name
         :in $ ?film
         :where
         [?e :movie/title ?film]
         [?e :movie/cast ?cast]
         [?cast :person/name ?name]]
       @conn "Lethal Weapon")

  ;; directors of films with Arnold
  (d/q '[:find ?title ?director-name
         :in $ ?actor-name
         :where
         [?a :person/name ?actor-name]
         [?m :movie/cast ?a]
         [?m :movie/director ?d]
         [?m :movie/title ?title]
         [?d :person/name ?director-name]]
       @conn "Arnold Schwarzenegger")



  ;; show changes; see https://github.com/replikativ/datahike/blob/main/doc/time_variance.md
  (d/q '[:find ?title ?tx
         :in $ ?id
         :where
         [?id :movie/title ?title ?tx]]
       (d/history @conn)
       silverster-movie-id)

  ;; the transactions are also just data and the time of tx...
  (sort-by second (d/q '[:find ?title ?t ?tx
                         :in $ ?mid
                         :where
                         [?mid :movie/title ?title ?tx]
                         [?tx :db/txInstant ?t]]
                       (d/history @conn)
                       silverster-movie-id))


  ;; show tx and disctracted data flag... true=added false=disctracted
  ;; so it's not only E A V but Transaction and Added-Flag
  (sort-by second (d/q '[:find ?title ?time ?tx ?added-or-distracted
                         :in $ ?mid
                         :where
                         [?mid :movie/title ?title ?tx ?added-or-distracted]
                         [?tx :db/txInstant ?time]]
                       (d/history @conn)
                       silverster-movie-id))

  ;; ignore the disctracted entries
  (sort-by second (d/q '[:find ?title ?t
                         :in $ ?mid
                         :where
                         [?mid :movie/title ?title ?tx ?added]
                         [?tx :db/txInstant ?t]
                         [(= true ?added)]]
                       (d/history @conn) silverster-movie-id))

  ;; find all attributes connected to transactions
  (d/q '[:find ?attrib-name
         :where
         [?tx :db/txInstant]
         [?tx ?attrib-name]]
       @conn)

  ;; use additional data
  (d/q '[:find ?m ?p ?title ?box-office
         :in $ ?director [[?title ?box-office]]
         :where [?p :person/name ?director]
         [?m :movie/director ?p]
         [?m :movie/title ?title]]
       @conn
       "Ridley Scott"
       [["Die Hard" 140700000]
        ["Alien" 104931801]
        ["Lethal Weapon" 120207127]
        ["Commando" 57491000]])


  ;; use predicate
  (d/q '[:find ?year ?title
         :where
         [?m :movie/title ?title]
         [?m :movie/year ?year]
         [(< ?year 1985)]]
       @conn)

  (def ratings [["Die Hard" 8.3]
                ["Alien" 8.5]
                ["Lethal Weapon" 7.6]
                ["Commando" 6.5]
                ["Mad Max Beyond Thunderdome" 6.1]
                ["Mad Max 2" 7.6]
                ["Rambo: First Blood Part II" 6.2]
                ["Braveheart" 8.4]
                ["Terminator 2: Judgment Day" 8.6]
                ["Predator 2" 6.1]
                ["First Blood" 7.6]
                ["Aliens" 8.5]
                ["Terminator 3: Rise of the Machines" 6.4]
                ["Rambo III" 5.4]
                ["Mad Max" 7.0]
                ["The Terminator" 8.1]
                ["Lethal Weapon 2" 7.1]
                ["Predator" 7.8]
                ["Lethal Weapon 3" 6.6]
                ["RoboCop" 7.5]])

  ;; find films with its directors and year of creation made between from-year and to-year with a rating of at least ?min-rating
  (sort-by first (d/q '[:find ?title ?director ?year ?rating
                        :in $ [[?title ?rating]] ?from-year ?to-year ?rating-at-least
                        :where
                        [?m :movie/year ?year]
                        [?m :movie/title ?title]
                        [?m :movie/director ?d]
                        [?d :person/name ?director]
                        [(<= ?year ?to-year)]
                        [(>= ?year ?from-year)]
                        [(>= ?rating ?rating-at-least)]]
                      @conn ratings
                      1989 1992 7.0))

  ;; average rating of all films between years from-year until to-year
  ;; aggregates could be: min max sum avg count ...
  (d/q '[:find (avg ?rating)
         :in $ [[?title ?rating]] ?from-year ?to-year ?rating-at-least
         :where
         [?m :movie/year ?year]
         [?m :movie/title ?title]
         [?m :movie/director ?d]
         [?d :person/name ?director]
         [(<= ?year ?to-year)]
         [(>= ?year ?from-year)]]
       @conn ratings
       1980 1989) ; compare with 1990 1999 ;-)

  ;; another example of aggregate and predicates
  (d/q '[:find [(count ?title) ...]
         :in $ ?from-year ?to-year
         :where
         [?m :movie/year ?year]
         [?m :movie/title ?title] ; try to remove title and do it with year only!
         [(<= ?year ?to-year)]
         [(>= ?year ?from-year)]]
       @conn
       1980 1989)

  ;; find and sort ratings of a director - may be better than max...
  (sort-by (comp - last)
           (d/q '[:find ?title ?director ?year ?rating
                  :in $ [[?title ?rating]] ?director
                  :where
                  [?m :movie/year ?year]
                  [?m :movie/title ?title]
                  [?m :movie/director ?d]
                  [?d :person/name ?director]]
                @conn ratings
                "James Cameron"))


  ;; do calc's age
  (defn calc-age [^java.util.Date birthday ^java.util.Date today]
    (quot (- (.getTime today)
             (.getTime birthday))
          (* 1000 60 60 24 365)))
  ;; test
  (calc-age (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd") "1969-07-14") (java.util.Date.))

  ;; use calc-age in query
  (d/q '[:find ?age
         :in $ ?name ?today
         :where [?p :person/name ?name]
         [?p :person/born ?born]
         [(bel-learn-chapters.60-datahike-ex-data/calc-age ?born ?today) ?age]]
       @conn
       "Tina Turner"
       (java.util.Date.))

  ;;
  ;; PULL API
  ;; (d/pull @conn selector entity-id)
  ;;

  (def movie-entity (first (d/q '[:find [?e ...]
                                  :where [?e :movie/title "Alien"]]
                                @conn)))
  (d/pull @conn ["*"]
          movie-entity)
  (d/pull @conn [:movie/title :movie/year
                 {:movie/director [:person/name]}]
          movie-entity)

  (defn show-tree [q-result]
    (FlatDarkLaf/install)
    (insp/inspect-tree q-result))

  (show-tree (d/pull @conn [:movie/title :movie/year
                            {:movie/cast [:person/name]}
                            {:movie/director [:person/stupid-wrong-attrib
                                              :person/name
                                              :person/born
                                              :person/death]}]
                     movie-entity))

  (def person-entity (first (d/q '[:find [?e ...]
                                   :where [?e :person/name "Richard Crenna"]]
                                 @conn)))
  (d/pull @conn [:person/name :person/born]
          person-entity)
  (d/pull @conn ["*"]
          person-entity)
  ;; entity lookup ref - you need uniqueness in schema
  (d/pull @conn [:person/name :person/born]
          [:person/name "Richard Crenna"])
  ;; reverse lookup ;-)
  (d/pull @conn [:person/name {:movie/_cast [:movie/title]}]
          [:person/name "Richard Crenna"])

  ;;
  ;; add and remove data, especially references
  ;;

  (d/transact conn [{:movie/title "Der mit dem Wolf tanzt"
                     :movie/year 1990
                     :movie/director -1
                     :movie/cast [-1 -2 -3 -4]}
                    {:db/id -1
                     :person/name "Kevin Costner"}
                    {:db/id -2
                     :person/name "Mary McDonnell"
                     :person/bourne 1960}
                    {:db/id -3
                     :person/name "Graham Greene"}
                    {:db/id -4
                     :person/name "Rodney A. Grant"}])


  (def update
    [[:db/add [:person/name "Kevin Costner"] :person/born #inst "1955-01-18T00:00:00.000-00:00"]
     [:db/retract [:person/name "Mary McDonnell"] :person/bourne 1960]])
  ; retraction of attribute needs the current value, too

  (d/pull @conn ["*"] [:person/name "Kevin Costner"])
  (d/pull @conn ["*"] [:person/name "Mary McDonnell"])
  (d/transact conn update)
  ;(d/transact conn {:tx-data update})
  (d/pull @conn ["*"] [:person/name "Kevin Costner"])
  (d/pull @conn ["*"] [:person/name "Mary McDonnell"])

  (def update2
    [[:db/retractEntity [:person/name "Mary McDonnell"]]])
  ;; just using the :tx-data notation. Could use vecor notation as well.
  (d/transact conn {:tx-data update2})
  ;; is removed from the cast, too!
  (def dmdwt (first (d/q '[:find [?e ...]
                           :where [?e :movie/title "Der mit dem Wolf tanzt"]]
                       @conn)))
  (d/pull @conn [{:movie/director [:person/name]} {:movie/cast [:person/name]}] dmdwt)
  (def update3
    [[:db/retractEntity [:person/name "Kevin Costner"]]])
  (d/transact conn {:tx-data update3})
  (d/pull @conn [{:movie/director [:person/name]} {:movie/cast [:person/name]}] dmdwt)

  nil)



