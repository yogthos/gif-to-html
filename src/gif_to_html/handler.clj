(ns gif-to-html.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [selmer.parser :as parser]
            [clj-http.client :as client]
            [gif-to-html.convert :refer [gif->html]]
            [noir.request :refer [*request*]]
            [noir.response :as response]
            [noir.util.middleware :refer [app-handler]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [site-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]))

(defn home-page [url]
  (parser/render-file
    "templates/home.html"
    {:url (or url "http://i.stack.imgur.com/e8nZC.gif")
     :servlet-context (:servlet-context *request*)
     :autorun (some? url)}))

(defn convert-url [url]
  (response/json
   (try
     (gif->html (:body (client/get url {:as :stream})))
     (catch Exception _
       {:error "The server was displeased with your offering (╯°□°)╯︵ ┻━┻"}))))

(defroutes routes
  (GET "/" req (home-page (:query-string req)))
  (POST "/convertImage" [url] (convert-url url))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (app-handler
   [routes]
   :ring-defaults
   (assoc-in site-defaults [:security :anti-forgery] false)))
