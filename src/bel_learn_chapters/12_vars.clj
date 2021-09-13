(ns bel-learn-chapters.12-vars)

(comment
  (def benno "Benno Loeffler")
  (println benno)

  (future
   (Thread/sleep 500)
   (println benno))

  (let [name benno]
   (future
    (Thread/sleep 700)
    (println name)))

  (alter-var-root #'benno (constantly "anderer Benno"))
  (def benno "anderer Benno-Name")
  (alter-var-root (var benno) (constantly "nochmal anderer Benno"))

;(set! benno "Benno Ralf Löffler")
;(binding [benno "Benno Ralf Löffler"]
;  (println benno))

  (def ^:dynamic sabine "SK")
  (alter-var-root #'sabine (constantly "Biene"))
  (binding [sabine "Sabine"]
    (println sabine)
    (set! sabine "neue Sabine")
    (println sabine)))
