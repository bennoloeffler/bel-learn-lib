(ns bel-learn-chapters.50-http
  (:require
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.util.response :refer [response not-found]]
    [ring.middleware.resource :refer [wrap-resource]]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.file :refer [wrap-file]]
    [ring.middleware.session :refer [wrap-session]]))

; see: https://www.baeldung.com/clojure-ring


(defn err-handler [request]
  (not-found "BELs file not found: "))

(def static-resource-handler
  (-> err-handler
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified))

(def static-file-handler
  (-> err-handler
      (wrap-file "public")
      wrap-content-type
      wrap-not-modified))


(defn handler [request]
  ;(println request)
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello s BELs World"}
  (response (:remote-addr request))
  #_(static-file-handler request))


(defn -main
  [& args]
  (run-jetty handler {:port 3000}))

(comment
  (ring.util.response/response "Hello, this is good")
  (ring.util.response/bad-request "Hello, this is bad")
  (ring.util.response/redirect "https://ring-clojure.github.io/ring/")
  (ring.util.response/created "/post/123")
  (ring.util.response/not-found "not found the BEL")
  (def r (ring.util.response/response "Hello, this is a response"))
  (ring.util.response/content-type r "no-content"))




