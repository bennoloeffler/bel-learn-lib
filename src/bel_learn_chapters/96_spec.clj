(ns bel-learn-chapters.96-spec
  (:require [clojure.spec.alpha :as s]))

(defrecord V [^double x ^double y])

(defn v?
  "is this of type V?"
  [o]
  (= V (type o)))

(s/def ::id (s/or ::number int? ::name string?))
(s/def ::position v?)
(s/def ::speed v?)
(s/def ::d0-1 (s/and double? #(<= 0 % 1)))

(s/def ::poisoned ::d0-1)
(s/def ::energy ::d0-1)
(s/def ::invisible ::d0-1)

(s/def ::moving-object (s/keys :req
                               [::id
                                ::position
                                ::speed
                                ::energy]))
;:opt [:acct/phone]))

(s/def ::pray (s/merge ::moving-object
                       (s/keys :req [::poisoned])))
(s/def ::hunter (s/merge ::moving-object
                         (s/keys :req [::invisible])))
(s/def ::hunter-player (s/* ::moving-object))
(s/def ::hunter-auto (s/* ::moving-object))
(s/def ::prays (s/* ::pray))
(s/def :game/world (s/keys :req
                           [::hunter-player
                            ::prays]))

(comment
  (s/explain ::moving-object {::id       12
                              ::position (V. 12 23)
                              ::speed    (V. 12 2)})
  (s/explain ::world
             {::hunter-player   {::id       12
                                 ::position (V. 12 23)
                                 ::speed    (V. 12 2)}
              ::prays [{::id       12
                        ::position (V. 12 23)
                        ::speed    (V. 12 2)
                        ::poisoned true}
                       {::id       12
                        ::position (V. 12 23)
                        ::speed    (V. 12 2)
                        ::poisoned false}]}))
