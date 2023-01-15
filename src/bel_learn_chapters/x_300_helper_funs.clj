(ns bel-learn-chapters.x-300-helper-funs
  (:require [tupelo.core :as tc]
            [medley.core :as mc]
            [com.rpl.specter :as sp]))

;; medley: https://github.com/weavejester/medley
;; tupelo: https://github.com/cloojure/tupelo
;;https://github.com/redplanetlabs/specter


(tc/const->fn 17)
(mc/abs -34)
(sp/transform [sp/MAP-VALS sp/MAP-VALS]
              inc
              {:a {:aa 1} :b {:ba -1 :bb 2}})
