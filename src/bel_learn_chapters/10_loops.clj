(ns bel-learn-chapters.10-loops)

(def my-items ["shirt" "coat" "hat"])

(comment

  (doseq [i my-items]
    (println i))

  (dotimes [i 10
            (println "counting:" i)]))
