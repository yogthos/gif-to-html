(ns gif-to-html.handler
  (:require [compojure.core :refer [defroutes]]
            [selmer.parser :as parser]
            [clj-http.client :as client]
            [gif-to-html.convert :refer [gif->html]]
            [noir.request :refer [*request*]]
            [noir.response :as response]
            [noir.util.middleware :refer [app-handler]]
            [compojure.core :refer [GET POST]]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
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
       (.printStackTrace _)
       {:error "The server was displeased with your offering (╯°□°)╯︵ ┻━┻"}))))

(defroutes app-routes
  (GET "/" req (home-page (:query-string req)))
  (POST "/convertImage" [url] (convert-url url))
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/appender-fn})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "gif_to_html_app.log" :max-size (* 512 1024) :backlog 10})

  (if (env :dev) (parser/cache-off!))
  (timbre/info "gif-to-html-app started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "gif-to-html-app is shutting down..."))

(def app (app-handler [app-routes]
                      :ring-defaults
                      (-> site-defaults
                          (assoc-in [:security :xss-protection :enable?] false)
                          (assoc-in [:security :anti-forgery] false))
                      :formats [:json-kw]))
