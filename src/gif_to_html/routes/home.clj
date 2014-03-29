(ns gif-to-html.routes.home
  (:use compojure.core)
  (:require [gif-to-html.views.layout :as layout]
            [gif-to-html.util :as util]
            [clj-http.client :as client]
            [gif-to-html.convert :refer [gif->html]]
            [noir.response :as response]))

(defn home-page [url]
  (layout/render
    "home.html" {:url (or url "http://i.stack.imgur.com/e8nZC.gif")}))

(defn convert-url [url]
  (response/json (gif->html (:body (client/get url {:as :stream})))))

(defroutes home-routes
  (GET "/" req (home-page (:query-string req)))
  (POST "/convertImage" [url] (convert-url url)))
