(ns bel-learn-chapters.x-165-rxclojure
  (:require [rx.lang.clojure.core :as rx]
            [tupelo.string :as str])
  (:import [rx Observable]
           [java.util.concurrent TimeUnit]))


(comment

  ; create observables

  (def obs (rx/return 10))
  (rx/subscribe obs #(prn (str "got value: " %)))

  (-> (rx/seq->o [1 2 3 4])
      (rx/subscribe prn))

  (-> (rx/range 1 5)
      (rx/subscribe prn))


  ; custom observable

  (defn just-obs-3 [v]
    (rx/observable*
      (fn [observer]
        (rx/on-next observer v)
        (rx/on-next observer v)
        (rx/on-next observer v)
        (rx/on-completed observer))))
  (rx/subscribe (just-obs-3 10) prn)


  ; unsubscribe

  (def obs2 (Observable/interval 100 TimeUnit/MILLISECONDS))
  (def sub (rx/subscribe obs2 prn))
  (rx/unsubscribe sub)


  ; filter and reduce on observable

  (rx/subscribe (->> (Observable/interval 1 TimeUnit/MICROSECONDS)
                     (rx/filter even?)
                     (rx/take 5)
                     (rx/reduce +))
                prn)


  ; map and mix

  (defn musicians []
    (rx/seq->o ["James Hetfield" "Dave Mustaine" "Kerry King"]))
  (defn bands []
    (rx/seq->o ["Metallica" "Megadeath" "Sayer"]))
  (defn uppercased-obs [obs]
    (rx/map (fn [s] (str/upper-case s)) obs))
  (def obs3 (-> (rx/map vector
                        (musicians)
                        (uppercased-obs (bands)))))
  (rx/subscribe obs3 (fn [[m b]] (println m "- from:" b)))


  ; flatmap (like mapcat)

  (defn factorial [n]
    (reduce * (range 1 (inc n))))

  (defn fact-obs [n]
    (rx/observable*
      (fn [observer]
        (rx/on-next observer (factorial n))
        (rx/on-completed observer))))

  (rx/subscribe (fact-obs 5) prn)

  (defn all-pos-ints []
    (Observable/interval 1 TimeUnit/MICROSECONDS))

  (rx/subscribe (->> (all-pos-ints)
                     (rx/filter even?)
                     (rx/flatmap fact-obs) ; makes several Observables to one flattended
                     (rx/take 5))
                prn)


  ; error handling or closing (see .retry)

  (defn an-ex-obs []
    (rx/observable*
      (fn [observer]
        (rx/on-next observer 3)
        (rx/on-next observer 36)
        ;(rx/on-completed observer) ;uncomment to close
        (rx/on-next observer (throw (ex-info "obs crashed" {:at-value 75}))))))
  (rx/subscribe (an-ex-obs)
                prn
                #(prn (str "error is: " %))
                #(prn "closed"))

  (rx/subscribe (->> (an-ex-obs)
                     (rx/catch Exception e ; do deliver another stream on Exception
                       (rx/seq->o [:a :b :c]))
                     (rx/map #(str "val: " %)))
                prn
                #(prn (str "error is: " %))
                #(prn "closed")))


  ; backpressure - see .onBackpresureBuffer, .sample, ...










