(ns bel-learn-chapters.12-vars)


; https://stackoverflow.com/questions/24951894/clojure-how-to-let-the-library-users-choose-which-printing-function-to-use-to/24955447#24955447

(comment

  (do
    (defn add-both [[a b]]
      ())
    (def non-atomic [0 0])
    (def f1 (future
              (doall (repeatedly 1000000 #(alter-var-root #'non-atomic inc)))))
    (def f2 (future
              (doall (repeatedly 1000000 #(alter-var-root #'non-atomic inc)))))
    @f2
    @f1
    ;(Thread/sleep 400)
    (println non-atomic)))

(do
  (def atomic (atom 0))
  (def f1 (future
            (doall (repeatedly 10000 #(swap! atomic inc)))))
  (def f2 (future
            (doall (repeatedly 10000 #(swap! atomic inc)))))
  @f2
  @f1
  (println @atomic))


(comment

  (def a 0)


  (def a 5)
  (def a (atom 5))
  (def ^:dynamic *a* (atom 5))

  @#'a
  @a
  (swap! a inc)
  @#'a
  @a
  (alter-var-root #'a (constantly (atom 20)))
  @#'a
  @a
  (binding [a (atom 17)]
    @#'a
    @a))


(comment
  (def person (atom {:id 15 :name "Benno" :age 12 :children ["Paul" "Benno" "Leo"]}))

  (defn change-person [person props-map]
    (into person props-map))

  (swap! person change-person {:age 45})
  (swap! person into {:age 50}))

(comment
  (do
    (def benno "Benno Loeffler")
    (println "1 -> " benno)

    (future
     (Thread/sleep 500)
     (println "2 -> " benno))

    (let [name benno] ; CREATE LOCAL BINDING - not affected from change
     (future
       (Thread/sleep 700)
       (println "3 -> " name)))

    (println "two threads running...")
    (alter-var-root #'benno (constantly "anderer Benno"))
    (def benno "anderer Benno-Name")
    (alter-var-root (var benno) (constantly "nochmal anderer Benno"))
    (println "4 now! -> " benno)))
;(set! benno "Benno Ralf Löffler")
;(binding [benno "Benno Ralf Löffler"]
;  (println benno))

(comment
  (do
     (def ^:dynamic sabine "SK")
     ;(set! sabine "S") ERROR - not for ROOT-BINDING
     (println sabine " --> " #(var sabine))
     (alter-var-root #'sabine (constantly "Biene"))
     (println sabine)
     (binding [sabine "Sabine"]
         (println sabine)
         (set! sabine "neue Sabine") ; ONLY for LOCAL BINDING
         (println sabine))))
