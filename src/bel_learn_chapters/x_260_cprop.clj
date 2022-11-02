(ns bel-learn-chapters.x-260-cprop
  (:require
    [puget.printer :refer  [cprint]]
    [clojure.pprint :refer [pprint]]
    [cprop.core :refer [load-config]]
    [cprop.source :refer [from-system-props
                          from-env]]))




(println)
(cprint :-----system-properties-------------------------------------------------)
(cprint (from-system-props))


(println)
(cprint :-----environment-------------------------------------------------------)
(cprint (from-env))

(println)
(cprint :-----my-config.edn-----------------------------------------------------)
(cprint (load-config :file "my-config.edn"))


#_(cprint :-----ALL-----)
#_(cprint (load-config :file "my-config.edn"
                       :merge [(from-system-props)
                               (from-env)]))