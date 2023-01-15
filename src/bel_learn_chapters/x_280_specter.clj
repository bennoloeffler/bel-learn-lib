(ns bel-learn-chapters.x-280-specter
  (:require [com.rpl.specter :as sp]))

;;https://github.com/redplanetlabs/specter

(sp/transform [sp/MAP-VALS sp/MAP-VALS]
           inc
           {:a {:aa 1} :b {:ba -1 :bb 2}})



