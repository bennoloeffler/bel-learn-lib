(ns bel-learn-chapters.10-loops)


(comment

  (def my-items ["shirt" "coat" "hat"])

  ; create list
  (for [i my-items]
    (keyword i))

  (doseq [i my-items]
    (println i))

  (dotimes [i 10]
           (println "counting:" i)))
