(ns gif-to-html.routes.home
  (:use compojure.core)
  (:require [gif-to-html.views.layout :as layout]
            [gif-to-html.util :as util]
            [clj-http.client :as client]
            [gif-to-html.convert :refer [gif->html]]
            [noir.response :as response]))

(defn home-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn convert-url [url]
  (response/json (gif->html (:body (client/get url {:as :stream})))))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/convertImage" [url] (convert-url url)))
