(ns bel-learn-chapters.97-malli-truss
  (:require [puget.printer :refer [cprint]]
            [clojure.pprint :refer [pprint]]
            [malli.core :as m]
            [malli.error :as me]
            [malli.dev.pretty :as pretty]
            [malli.util :as mu]
            [malli.transform :as mt]
            [malli.generator :as mg]
            [criterium.core :as cc]
            [malli.provider :as mp]
            [malli.destructure :as md]
            [clojure.test.check.generators :as gen]
            [malli.dot :as mdot]
            [malli.instrument :as mi]
            [malli.experimental :as mx]
            [malli.dev :as dev]
            [malli.experimental.time :as met]
            [malli.registry :as mr]
            [tick.core :as t]))



;; alternatives to spec
;; https://github.com/ptaoussanis/truss
;; https://github.com/metosin/malli
;; https://github.com/plumatic/schema
;; https://github.com/fulcrologic/guardrails

; malli, cool intros
; https://www.metosin.fi/blog/malli
; https://ostash.dev/posts/2021-08-22-data-validation-in-clojure/

; malli: all examples
; also: auth, swagger API
; https://github.com/metosin/reitit/tree/master/examples

; create EVERYTHING from malli
; https://github.com/dvingo/malli-code-gen/blob/main/thoughts.md

(comment
  (use 'debux.core)


  ;  ---------- examples of schemas ----------

  ;; just a type (String)
  :string

  ;; type with properties
  [:string {:min 1, :max 10}]

  ;; type with properties and children
  [:tuple {:title "location"} :double :double]

  ;; a function schema of :int -> :int
  [:=> [:cat :int] :int]

  ; ---------- usage ----------

  (def non-empty-string
    (m/schema [:string {:min 1}]))

  (m/schema? non-empty-string)
  ; => true

  (m/validate non-empty-string "")
  ; => false

  (m/validate non-empty-string "kikka")
  ; => true

  ; not just the data - but a Schema object!
  (assert (not=
            (m/schema [:string {:min 1}])
            [:string {:min 1}]))

  (type (m/schema [:string {:min 1}]))

  ; m/form gives the data
  (assert (=(m/form non-empty-string)
            [:string {:min 1}]))

  ; ----------- schema AST ----------
  (m/ast non-empty-string)
  ; map-form:
  ; {:type :string,
  ;  :properties {:min 1}}

  ;; optimized (pure) validation function for best performance
  (def valid?
    (m/validator
      [:map
       [:x :boolean]
       [:y {:optional true} :int]
       [:z :string]]))

  (valid? {:x true, :z "kikka"}))


;; MY DOMAIN - first try...
(comment
  (def schema-model
    (m/schema
      [:map
       [:resources [:map-of :int [:map
                                  [:name :string]
                                  [:capa [:vector :int]]]]]
       [:tasks [:map-of :int [:map
                              [:start :int]
                              [:end :int]
                              [:capa :int]
                              [:resource-ref :int]]]]]))

  (m/validate schema-model
              {:resources {1 2}
               :tasks {}})

  ;; where is LocalDate? for start and end?
  ;; starting with ints...
  (m/validate schema-model
              {:resources {1 {:name "r1"
                              :capa [1 2 3]}}
               :tasks {1 {:start 1
                          :end 2
                          :capa 5
                          :resource-ref 1}}})

  ;; play with errors
  (-> schema-model
      (m/explain {:resources {1 {:name "r1"
                                 :capa [1 2 3]}}
                  :tasks {1 {;:start 1
                             :end 2
                             :capa "5"
                             :resource-ref 1}}})
      (me/humanize)
      (cprint {:width 40})))


(def my-fn-schema
  [:and
   [:map
    [:x int?]
    [:y int?]]
   [:fn (fn x-should-be-bigger-then-y [{:keys [x y]}] (> x y))]])

(m/validate my-fn-schema {:x 1, :y 0})
; => true

; hmmmmm.... unknown error - what???
(me/humanize (m/explain my-fn-schema {:x 1, :y 2}))

(def my-fn-schema
  [:and
   [:map
    [:x int?]
    [:y int?]]
   [:fn
    {:error/message "x should be bigger than y"}
    (fn [{:keys [x y]}] (> x y))]])

; better
(me/humanize (m/explain my-fn-schema {:x 1, :y 2}))

(def my-fn-schema
  [:and
   [:map
    [:x int?]
    [:y int?]]
   [:fn
    ;; parameters for error/fn is in :value with keys of the map
    {:error/fn (fn [{{:keys [x y]} :value} _] (str ":x=" x " should be bigger than :y="y))};
    ; "\nval="val"\nval2="val2))}
    (fn [{:keys [x y]}] (> x y))]])

; good
(me/humanize (m/explain my-fn-schema {:x 1, :y 2}))

(comment
  (cprint
    (-> [:map
         [:id int?]
         [:size [:enum {:error/message "should be: S|M|L"}
                 "S" "M" "L"]]
         [:age [:fn {:error/fn (fn [{:keys [value]} _] (str value ", should be > 18"))}
                (fn [x] (and (int? x) (> x 18)))]]]
        (m/explain {:size "XL", :age 10})
        (me/humanize

          ;; change default error messages
          {:errors (-> me/default-errors
                       (assoc ::m/missing-key {:error/fn (fn [{:keys [in]} _] (str "missing key " (last in)))}))}))))

;{:id ["missing key :id"]
; :size ["should be: S|M|L"]
; :age ["10, should be > 18"]}

;;
;; closed map - check misspelled keys
;;
(comment
  (pprint
    (-> [:map [:address [:map [:street string?]]]]
        (mu/closed-schema)
        (m/explain
          {:name "Lie-mi"
           :address {:streetz "Hämeenkatu 14"}})
        (me/with-spell-checking)
        (me/humanize))))

(def Address
  [:map
   [:id string?]
   [:tags [:set keyword?]]
   [:address
    [:map
     [:street string?]
     [:city string?]
     [:zip int?]
     [:lonlat [:tuple double? double?]]]]])

;
; show only errors - leave out valid values
;

(comment
  (-> Address
      (m/explain
        {:id "Lillan"
         :tags #{:artesan "coffee" :garden "ground"}
         :address {:street "Ahlmanintie 29"
                   :zip 33100
                   :lonlat [61.4858322, "23.7832851,17"]}})
      (me/error-value {::me/mask-valid-values '...})
      cprint))
; the trick... [:id '...]

;; registry with decomplected elements
(me/humanize
  (m/explain
    [:map {:registry {::id int?
                      ::country string?}}
     ::id
     [:name string?]
     [::country {:optional true}]]
    {::id :1
     :name "kikka"}))

;; ::m/default "closes" the map!
(->(m/explain
     [:map
      [:x :int]
      [:y :int]
      [::m/default [:map-of :int :int]]]
     {:x 1, :y 2, :z 1, 2 2})
   (me/humanize))

;; nesting does not nest the map, it
;; just extends the defaults by AND.
;; it reads:
;  default map is :int :int
;  and default is :z   :string
;  and default is :y   :int
;
;  Look at the error messages!
;
(->
  [:map
   [:x :string]
   [::m/default [:map
                 [:y :int]
                 [::m/default [:map
                               [:z :string]
                               [::m/default [:map-of :int :int]]]]]]]
  (m/explain {:x 1, :y 2, :z 1, 2 2, 3 "str" :a 1})
  (me/humanize))

; sequence schemas
(m/validate [:sequential :any]
            '(1 :x "str" true))

(m/validate [:vector :int]
            [1 23])

(m/validate [:tuple :int :string :boolean]
            [1 "23" false])

; type error
(-> [:sequential :any]
   (m/explain #{1 2 3})
   (me/humanize))

; positional error
(-> [:vector :int]
    (m/explain [1 23 :a])
    (me/humanize))
;;
;; little helper...
;;
(defn hum-err [schema data]
 (-> schema
     #_(malli.dev.pretty/explain data)
     (m/explain data)
     (me/humanize)))

; tuple error
(hum-err [:tuple :int :string :boolean]
         [1 "23" :false])

(defn hum-err-pr [schema data]
  (-> schema
      (malli.dev.pretty/explain data)
      #_(m/explain data)
      #_(me/humanize)))

; tuple error
(comment
  (hum-err-pr [:tuple :int :string :boolean]
              [1 "23" :false]))


(defn hum-err-mult
  "little helper to test
  malli schemas against data and
  see the result of a complete
  data set against one schema.
  For better readability:
  require [puget.printer :refer [cprint]]
  and use it with hum-err-mult.
  E.g.
  (cprint
    (hum-err-mult
      [:vector :int]

      [2 3 4]
      [1 :wrong 3]
      []
      [1 (count [:x :y])])"
  [schema & datas]
  (vec (map-indexed
         (fn [idx data] (let [msg (hum-err schema data)]
                          (if msg
                            [(str (inc idx) "-failed:") :data data :msg msg]
                            [(keyword (str (inc idx) "-passed."))])))
         datas)))

(comment
  (cprint (hum-err-mult
            [:vector :int]

            [2 3 4]
            [1 :wrong 3]
            []
            [1 (count [:x :y])])))

;;
;; regex-like operators
;;
;; :cat :catn :alt :altn :? :* :+ :repeat
;; :schema to force childs to behave like new schema
;;
;; SLOW! AVOID IT WHEN POSSIBLE.

(comment
  (let [valid? (m/validator [:* int?])]
    (cc/quick-bench (valid? (range 10))))
  ;; 1100 to 1700 ns (nano seconds) = 1,1 to 1,7 µs (micro seconds)
  ;; lets say 1300 ns in average

  (let [valid? (m/validator [:sequential int?])]
    (cc/quick-bench (valid? (range 10))))
  ;; 55,7 ns (nano seconds)

  ;; how much faster is the second option?
  (long (/ 1300 55.7)))
  ; 23 times faster


(m/validate
  [:cat [:= :names] [:schema [:* string?]] [:= :nums] [:schema [:* number?]]]
  [:names ["a" "b"] :nums [1 2 3]])

;; whereas
(m/validate
  [:cat [:= :names] [:* string?] [:= :nums] [:* number?]]
  [:names "a" "b" :nums 1 2 3])

(m/explain [:cat :int [:alt
                       :string
                       :keyword]]
           [17 12 #_:Benno])

; :catn gives better hints
(m/explain [:catn
            [:age :int]
            [:nick-or-name [:altn
                            [:name :string]
                            [:nick :keyword]]]]
           [17 12 #_:Benno])


(comment
  (cprint
    (hum-err-mult
      [:catn [:age :int] [:name-or-nick [:altn
                                         [:name :string]
                                         [:nick :keyword]]]]
      [17 :Benno]
      [17 15]
      [17 "Benno"]
      [17 "Benno" :Benno])))

(hum-err-mult
  [:* [:cat :int [:alt :string :keyword]]]
  [17 :Benno]
  []
  [17 "Benno"]
  [17 "Benno" 1 :Benno]
  [17 "Benno" 1 :Benno 1 false])

(hum-err-mult
  ;; strange error messages...
  [:repeat {:min 1 :max 2}
   [:catn [:age :int] [:schema [:altn
                                [:name :string]
                                [:nick :keyword]]]]]
  [17 :Benno]
  []
  [17 "Benno"]
  [17 "Benno" 1 :Benno]
  [17 "Benno" 1 :Benno 1 :Karl])

;;
;; strings and regex
;;
(comment
  (hum-err-mult
    :string ;; or string? or [:string]

    :no-string
    ""
    true
    (str 1))

  (hum-err-mult
    [:string {:min 2 :max 4}]

    :no-string
    "1"
    "very long"
    (str 123)))

(comment
  ;; [:re "..."] or plain #""
  ;; ^$ for complete matching
  (me/humanize (m/explain [:re #"Benno.*Sabine"] "111Bennos...X..Sabinec"))

  (cprint (hum-err-mult
            #"a+" ; contains one or more a

            ""
            "a"
            "ab"
            "xxxaaaaa"
            "aa"
            "xx"))

  ; ^ and $ make a difference!
  (cprint (hum-err-mult
            #"^a+$" ; is one or more a

            ""
            "a"
            "ab"
            "xxxaaaaa"
            "aa"
            "xx"))

  (hum-err-mult
    [:re "^a+$"] ;; SAME as above

    ""
    "a"
    "ab"
    "xxxaaaaa"
    "aa"
    "xx"))

;;
;; nilable
;;
(m/validate [:maybe string?] nil)
(m/validate [:maybe string?] "abc")
(m/validate [:maybe :int] 3)
(m/validate [:maybe :int] nil)

;;
;; get the most intuitive explanation
;;
(comment
  (me/humanize (malli.dev.pretty/explain
                 [:map {:registry {::id int?
                                   ::country string?}}
                  ::id
                  [:name string?]
                  [::country {:optional true}]]

                 {::id :1
                  :name "kikka"})))

;;
;; transformers / coercers / defaults
;;

; from str to int
(m/decode :int "42" mt/string-transformer)
; from int to str
(m/encode :int 42 mt/string-transformer)

;; performance
(def decode (m/decoder int? mt/string-transformer))
(decode "42")

(comment
  (def str-to-int (m/coercer :int mt/string-transformer))
  (def str-to-bool (m/coercer :boolean mt/string-transformer))

  (str-to-int "bla") ;fails. transform and validates
  (str-to-bool "false"))

;; see
;; https://github.com/metosin/malli#advanced-transformations

; default values
(m/decode [:and {:default 42} int?] nil mt/default-value-transformer)
;; see
;; https://github.com/metosin/malli#default-values

(m/encode
  [:map {:default {}}
   [:a [int? {:default 1}]]
   [:e int?]]
  nil
  (mt/transformer
    mt/default-value-transformer
    mt/string-transformer))

(me/humanize
  (m/explain
    ; so encoding is
    ; needed for defaults
    [:map {:default {}}
     [:a [int? {:default 1}]]
     [:e int?]]
    {:e 2}))

;;
;; programming with schemas
;; see: https://github.com/metosin/malli#programming-with-schemas
;;

(mu/update-properties [:vector int?] assoc :min 1)

(def abcd
  [:map {:title "abcd"}
   [:a int?]
   [:b {:optional true} int?]
   [:c [:map
        [:d int?]]]])

;; closing a schema and
;; get the data back
(m/form (mu/closed-schema abcd))

;;
;; dispatching types
;; see:https://github.com/metosin/malli#multi-schemas
;;
(hum-err-mult
  [:multi {:dispatch :type}; Any function can be used for :dispatch
   [:sized [:map [:type keyword?] [:size int?]]]
   [:human [:map [:type keyword?] [:name string?] [:address [:map [:country keyword?]]]]]]
   ;[::m/default :any]] ; only last would fail with :some
  {:type :sized, :size 10}
  {:type :sized, :size-x 10 :other 2}
  {:type :human :name "Karl" :address {:country :india}}
  {:type :unknown :bullshit "Karl" :some "stuff" :where {:country :india}}
  {}
  nil)

;;
;; Recursion with registry
;;

(comment
  ; Without the :ref keyword, malli eagerly expands
  ; the schema until a stack overflow error is thrown
  (def recursive-schema-with-local-registry
    [:schema {:registry
              {::cons [:maybe [:tuple pos-int? [:ref ::cons]]]}}
     [:ref ::cons]])

  (hum-err-mult recursive-schema-with-local-registry
                [12 [11 [12 nil]]]
                [12 [11 [12]]]
                [12 [11 [12 13]]]
                [12 [11 [nil]]])
  (hum-err-mult
    [:schema {:registry {::ping [:tuple [:= "ping"] [:ref ::pong]]
                         ::pong [:or
                                 [:tuple [:= "pong"] [:ref ::ping]]
                                 [:= "pong"]]}}
     ::ping]
    ["ping" ["pong" ["ping" ["pong" ["ping" "pong"]]]]]
    ["pong" ["ping" ["pong" ["ping" "pong"]]]] ; starts with pong
    ["ping" ["pong" "ping"]])) ; ends with ping...

;;
;; value generation
;;
(comment

  ; from 0 to max size?
  (mg/generate :string {:size 10})

  ; yes...
  (-> (map (fn [val] (count (mg/generate :string {:size 10})))
           (range 1000))
      frequencies
      keys
      sort)

  (mg/generate keyword?)
  (mg/generate [:enum "a" "b" "c"] {:seed 42})

  (mg/generate [:int {:min 0, :max 20}])
  (assert (= (range 0 21)
             (-> (map (fn [val] (mg/generate [:int {:min 0, :max 20}]))
                      (range 1000))
                 frequencies
                 keys
                 sort)))

  (mg/generate Address {:seed 123, :size 4})

  (mg/sample [:and int? [:> 10] [:< 100]]
             {:size 3 :seed 123})

  ;; BUT, still a TODO:

  (mg/sample [:and int? [:> 10] [:< 100]]
             {:size 300 :seed 123})

  ; NO not so simple...
  #_(defn age-gen []
      (+ 18 (rand-int 112)))

  ; uses SPEC
  (def age-gen
    (gen/fmap (fn [_] (+ 18 (rand-int 112)))
              (gen/return 1)))

  ;; works - but SPEC
  (gen/sample age-gen)

  ;; works. but still a generator by SPEC
  (mg/sample [int? {:gen/gen age-gen}])

  ;; THIS IS IT! Simple. But no fn as generator :-(
  (def Adult
    [:map {:registry {::age [:int {:min 18 :max 130}]}}
     ;; int? wont work
     [:age ::age]])
  (mg/sample Adult {:size 1000})

  ;; NOT SERIALIZABLE - but works for static use for fn
  (def Adult2
    [:map {:registry {::age [:int
                             {:gen/fmap
                              (fn [val]
                                ;(println val) ;value from :int
                                (+ 10 (rand-int 100)))}]}}
     [:age ::age]])

  (double (with-precision 3 (/ 10 3M)))

  (as-> Adult2 $
      (mg/sample $ {:size 100000})
      (map :age $)
      (frequencies $) ; show the histogram over ages
      (sort $)
      (map (fn [[age num-of-age]]
             [age
              num-of-age
              (-> (with-precision 4 (-> num-of-age ; round only Ms
                                        (- 1000) ; deviation
                                        (/ 1000) ; in 0..1=100%
                                        (* 100.0M))) ; deviation in %
                  double)])
           $)
      (sort-by #(get % 2) $)) ; sort by deviation in %

  ;; SERIALIZABLE - but needs sci or cherry
  (def Adult3
    [:map {:registry {::age [:int
                             {:gen/fmap
                              '(fn [val]
                                 (+ 20 (rand-int 100)))}]}}
     [:age ::age]])

  (mg/sample Adult3 {:size 1000}))


;;
;; inferring schemas
;;
(comment

  (def user
    {:name "tommi"
     :age 45
     :address {:street "Hämeenkatu"
               :country "Finland"}})
  (mp/provide [user])


  (def samples

     [{:projects ^{::mp/hint :map-of}
                 {1 {:id 1
                     :name "xy"
                     :sequence-num 1
                     :end #inst "2023-04-14"
                     :tasks ^{::mp/hint :map-of}
                     {3 {:start #inst "2023-04-14"
                         :end #inst "2023-04-14"
                         :resource-id 12
                         :capa-need 34.5}
                      5 {:start #inst "2023-04-14"
                         :end #inst "2023-04-14"
                         :resource-id 124
                         :capa-need 354.5}}}
                  4 {:id 4
                     :name "asdfxy"
                     :sequence-num 2
                     :end #inst "2023-05-14"
                     :tasks {3 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 124
                                :capa-need 344.5}
                             5 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 1324
                                :capa-need 3455}}}
                  3 {:id 3
                     :name "asdfxy"
                     :sequence-num 2
                     :end #inst "2023-05-14"
                     :tasks {3 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 124
                                :capa-need 344.5}
                             5 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 1324
                                :capa-need 3455}}}}
       :resources ^{::mp/hint :map-of}
       {6 {:id 6
           :name "sdads"
           :sequence-num 2
           :capa [{:cw 0 :yellow 100 :red 200}
                  {:cw 289 :yellow 200 :red 400}
                  {:cw 34 :yellow 140 :red 200}
                  {:cw 344 :yellow 14 :red 20}]}
        4 {:id 4
           :name "sdads"
           :sequence-num 2
           :capa [{:cw 0 :yellow 100 :red 200}
                  {:cw 289 :yellow 200 :red 400}
                  {:cw 34 :yellow 140 :red 200}
                  {:cw 344 :yellow 14 :red 20}]}
        5 {:id 5
           :name "ssadfdads"
           :sequence-num 3
           :capa [{:cw 0 :yellow 100 :red 200}
                  {:cw 289 :yellow 200 :red 400}
                  {:cw 34 :yellow 140 :red 200}
                  {:cw 344 :yellow 14 :red 20}]}}}
      {:projects {1 {:id 1
                     :name "xy"
                     :sequence-num 1
                     :end #inst "2023-04-14"
                     :tasks {3 {:start #inst "2023-04-14"
                                :end #inst "2023-04-14"
                                :resource-id 12
                                :capa-need 34.5}
                             5 {:start #inst "2023-04-14"
                                :end #inst "2023-04-14"
                                :resource-id 124
                                :capa-need 354.5}}}
                  4 {:id 4
                     :name "asdfxy"
                     :sequence-num 2
                     :end #inst "2023-05-14"
                     :tasks {3 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 124
                                :capa-need 344.5}
                             5 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 1324
                                :capa-need 3455}}}
                  3 {:id 3
                     :name "asdfxy"
                     :sequence-num 2
                     :end #inst "2023-05-14"
                     :tasks {3 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 124
                                :capa-need 344.5}
                             5 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 1324
                                :capa-need 3455}}}}
       :resources {6 {:id 6
                      :name "sdads"
                      :sequence-num 2
                      :capa [{:cw 0 :yellow 100 :red 200}
                             {:cw 289 :yellow 200 :red 400}
                             {:cw 34 :yellow 140 :red 200}
                             {:cw 344 :yellow 14 :red 20}]}
                   4 {:id 4
                      :name "sdads"
                      :sequence-num 2
                      :capa [{:cw 0 :yellow 100 :red 200}
                             {:cw 289 :yellow 200 :red 400}
                             {:cw 34 :yellow 140 :red 200}
                             {:cw 344 :yellow 14 :red 20}]}
                   5 {:id 5
                      :name "ssadfdads"
                      :sequence-num 3
                      :capa [{:cw 0 :yellow 100 :red 200}
                             {:cw 289 :yellow 200 :red 400}
                             {:cw 34 :yellow 140 :red 200}
                             {:cw 344 :yellow 14 :red 20}]}}}

      {:projects {1 {:id 1
                     :name "xy"
                     :sequence-num 1
                     :end #inst "2023-04-14"
                     :tasks {3 {:start #inst "2023-04-14"
                                :end #inst "2023-04-14"
                                :resource-id 12
                                :capa-need 34.5}
                             5 {:start #inst "2023-04-14"
                                :end #inst "2023-04-14"
                                :resource-id 124
                                :capa-need 354.5}}}
                  4 {:id 4
                     :name "asdfxy"
                     :sequence-num 2
                     :end #inst "2023-05-14"
                     :tasks ^{::mp/hint :map-of}
                     {3 {:start #inst "2023-06-14"
                         :end #inst "2023-07-14"
                         :resource-id 124
                         :capa-need 344.5}
                      5 {:start #inst "2023-06-14"
                         :end #inst "2023-07-14"
                         :resource-id 1324
                         :capa-need 3455}}}
                  3 {:id 3
                     :name "asdfxy"
                     :sequence-num 2
                     :end #inst "2023-05-14"
                     :tasks {3 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 124
                                :capa-need 344.5}
                             5 {:start #inst "2023-06-14"
                                :end #inst "2023-07-14"
                                :resource-id 1324
                                :capa-need 3455}}}}
       :resources {6 {:id 6
                      :name "sdads"
                      :sequence-num 2
                      :capa [{:cw 0 :yellow 100 :red 200}
                             {:cw 289 :yellow 200 :red 400}
                             {:cw 34 :yellow 140 :red 200}
                             {:cw 344 :yellow 14 :red 20}]}
                   4 {:id 4
                      :name "sdads"
                      :sequence-num 2
                      :capa [{:cw 0 :yellow 100 :red 200}
                             {:cw 289 :yellow 200 :red 400}
                             {:cw 34 :yellow 140 :red 200}
                             {:cw 344 :yellow 14 :red 20}]}
                   5 {:id 5
                      :name "ssadfdads"
                      :sequence-num 3
                      :capa [{:cw 0 :yellow 100 :red 200}
                             {:cw 289 :yellow 200 :red 400}
                             {:cw 34 :yellow 140 :red 200}
                             {:cw 344 :yellow 14 :red 20}]}}}])

  (mp/provide samples
              {::mp/map-of-threshold 3}))


;;
;; destructuring
;;
(comment
  (def infer (comp :schema md/parse))
  (infer '[a b & cs])
  (infer '[a :- :int, b :- :string & cs :- [:* :boolean]])

  (defn kikka
    ([a] [a])
    ([a b & cs] [a b cs]))

  (md/infer #'kikka))

;;
;; parsing
;;

(comment
  (m/parse
    [:* [:catn
         [:prop string?]
         [:val [:altn
                [:s string?]
                [:b boolean?]]]]]
    ["-server" "foo" "-verbose" true "-user" "joe"])

  ; getting tags for :orn :catn :altn and :multi
  (def Multi
    [:multi {:dispatch :type}
     [:user [:map [:size :int]]]
     [::m/default :any]])

  (m/parse Multi {:type :user, :size 1})
  ; => [:user {:type :user, :size 1}]

  (m/parse Multi {:type "sized", :size 1}))
  ; => [:malli.core/default {:type "sized", :size 1}]

;;
;; registry
;;

(comment
  ;; default registry as explicit option
  (m/validate [:maybe string?]
              "kikka"
              {:registry m/default-registry})

  (def my-registry
    (merge
      (m/class-schemas)
      (m/comparator-schemas)
      (m/base-schemas)
      {:pos-int (m/-simple-schema
                  {:type :pos-int, :pred pos-int?})
       ; pos-int? :pos-int ; enable pos-int?
       :neg-int (m/-simple-schema
                  {:type :neg-int, :pred neg-int?})}))


  ;; not positive or negative...
  (m/validate [:or :pos-int :neg-int]
              0
              {:registry my-registry})

  ;; pos-int? is NOT registered! only :pos-int!
  (m/validate pos-int? 123 {:registry my-registry})
  (m/validate :pos-int 123 {:registry my-registry})

  ;; local registry
  (def Adult
    [:map {:registry {::age [:and int? [:> 18] [:< 130]]}}
     [:age ::age]])
  (mg/generate Adult)
  (mg/sample Adult {:size 100})

  ;; add to the global registry:
  ;; put time schemas in default registry
  (def reg (let [min-date (t/date "2020-01-01")
                 max-date (t/date "2030-12-31")]
             (mr/set-default-registry!
               (mr/composite-registry
                 (m/default-schemas)
                 (met/schemas)
                 {:time/restricted-local-date
                  [:time/local-date
                   {:error/fn
                    (fn [{:keys [value]
                                ;[_ {:keys [min max]}] :schema ; WONT WORK. :schema is no map, but a Schema object
                          :as all} _]
                      ;(println (-> all :schema m/form (get 1)))
                      (let [max (-> all :schema m/form (get 1) :max)
                            min (-> all :schema m/form (get 1) :min)]
                        (if (t/date? value)
                          (str "date " value " should be between " min " and "max)
                          (str value " has wrong type: " (type value) ". Should satisfy tick/date? (cljs: joda local date, clj: LocalDate)"))));
                    :min      min-date
                    :max      max-date
                    :gen/fmap (fn [all]
                                (println all)
                                (let [start     (.toEpochDay min-date)
                                      end       (.toEpochDay max-date)
                                      epoch-day (->
                                                  (- end start)
                                                  rand-int
                                                  (+ start))]
                                  (t/new-date epoch-day)))}]}))))

  (m/validate :time/restricted-local-date (t/date))

  (me/humanize
    (m/explain :time/restricted-local-date
               "2013-04-15"))
  (me/humanize
    (m/explain :time/restricted-local-date
               (t/date "2019-12-24")))
  (mg/sample :time/restricted-local-date {:size 100}))

;;
;; visual
;; https://graphviz.org/about/
;; see: https://dreampuf.github.io/GraphvizOnline
;;

(def Order
  [:schema
   {:registry {"Country" [:map
                          [:name [:enum :FI :PO]]
                          [:neighbors [:vector [:ref "Country"]]]]
               "Burger" [:map
                         [:name string?]
                         [:description {:optional true} string?]
                         [:origin [:maybe "Country"]]
                         [:price pos-int?]]
               "OrderLine" [:map
                            [:burger "Burger"]
                            [:amount int?]]
               "Order" [:map
                        [:lines [:vector "OrderLine"]]
                        [:delivery [:map
                                    [:delivered boolean?]
                                    [:address [:map
                                               [:street string?]
                                               [:zip int?]
                                               [:country "Country"]]]]]]}}
   "Order"])

(mdot/transform Order)

;;
;; functions
;;

; no...
#_(m/defn bel-fun :- [:tuple int? int?]
          "returns two and gets one ints"
          [x :- int?]
          [x (* x x)])
(comment
  #_(mi/instrument!)
  ;better
  (dev/start!)
  (mx/defn plus-args :- :int
           [x :- :int
            y :- :int]
    (+ x y))

  (plus-args :1 2)

  (m/=> mult-small [:=>
                    [:cat [:int {:max 10}] [:int {:max 10}]]
                    [:int]])

  (defn mult-small [x y]
    (* x y))
  (mult-small 20 2))


;;
;; kondo
;;
(comment
  (defn plus1
    "Adds one to the number"
    {:malli/schema [:=> [:cat :int] :int]}
    [x] (inc x))

  ;; instrument, clj-kondo + pretty errors
  (require '[malli.dev :as dev])
  (require '[malli.dev.pretty :as pretty])
  (dev/start! {:report (pretty/reporter)})

  (plus1 "123")

  (dev/stop!))

;;
;; registry
;;



;;
;; local-date and tick
;; see: https://github.com/metosin/malli#malliexperimentaltime
;;

(comment
  (mr/set-default-registry!
    (mr/composite-registry
      (m/default-schemas)
      (met/schemas)))

  (.toEpochDay (t/date "2025-01-01"))

  (def date-schema [:time/local-date {:min (t/date "2020-01-01")
                                      :max (t/date "2030-12-31")
                                      :gen/fmap (fn [val] (let [start (.toEpochDay (t/date "2022-01-01"))
                                                                end (.toEpochDay (t/date "2023-01-01"))
                                                                epoch-day (+ start
                                                                             (rand-int (- end
                                                                                          start)))]
                                                            (t/new-date epoch-day)))}])
  ;; error is missleading
  (hum-err date-schema
           (t/date "2031-01-01"))

  (-> date-schema
      (mg/sample {:size 10000})
      (frequencies)
      (sort)))