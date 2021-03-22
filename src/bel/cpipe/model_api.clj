(ns bel.cpipe.model-api)

(defprotocol model-api
  "save and load model"
  (save [] "bar docs")
  (load [] "baz docs"))