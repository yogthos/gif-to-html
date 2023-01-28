(ns user
  (:require [hato.client :as hc]
            [gif-to-html.convert :refer [gif->html]]
            [ring.middleware.defaults :as defaults]
            [ruuter.core :as ruuter]
            [ring.adapter.jetty :as jetty]
            [hiccup.core :as hiccup]))

(defn home-page []
  [:html 
   [:body
    [:form {:action "/"
            :method :post}
     [:label {:for :url} "enter a URL for a GIF to be converted to ASCII"]
     [:br]
     [:input {:type :text
              :name :url
              :id   :url
              :value "https://media.tenor.com/JMzBeLgNaSoAAAAj/banana-dance.gif"}]
     [:br]
     [:input {:type :submit}]]]])
  
  (defn gif-animation [{:keys [frames frame-count delay]}]
    [:html 
     [:body
      [:div
       (map-indexed 
        (fn [i frame]
          [:pre
           {:id    (str "frame-" i)
            :style "font-size:4pt;line-height:4pt;letter-spacing:1px;font-weight:bold;display:none;font-family:monospace;"}
           frame])
        frames)]
      [:script {:type "text/javascript"}
       "var delay = " delay ";"]
      [:script {:type "text/javascript"}
       "var totalFrames = " (dec frame-count) ";"]
      [:script {:type "text/javascript"}
       "var animation;
        function showNextFrame(frame) {
          document.getElementById('frame-' + ((frame > 0) ? frame - 1 : totalFrames)).style.display = 'none';
          document.getElementById('frame-' + frame).style.display = 'block';;
          animation = setTimeout(function(){showNextFrame((frame < totalFrames) ? frame + 1 : 0);}, delay);}
        showNextFrame(0);"]]])
  
(def routes [{:path "/"
              :method :get
              :response (fn [_request] 
                          {:status 200
                           :body   (hiccup/html (home-page))})}
             {:path "/"
              :method :post
              :response (fn [{{:strs [url]} :form-params}] 
                          (try
                            (let [animation (gif->html (:body (hc/get url {:as :stream})))]
                              {:status 200
                               :body   (hiccup/html
                                        [:html
                                         [:body
                                          (gif-animation animation)]])})
                            (catch Exception e
                              
                              {:status 500
                               :headers {"Content-type" "text/html; charset=utf-8'"}
                               :body [:html
                                      [:body
                                       [:p "The server was displeased with your offering (╯°□°)╯︵ ┻━┻"]
                                       [:pre
                                        (with-out-str (.printStackTrace e))]]]})))}])

(defn run-jetty []
  (jetty/run-jetty (-> (partial ruuter/route routes)
                       (defaults/wrap-defaults
                        {:params    {:urlencoded true
                                     :multipart  true
                                     :nested     true
                                     :keywordize true}
                         :static    {:resources "public"}
                         :responses {:not-modified-responses true
                                     :absolute-redirects     false
                                     :default-charset        "utf-8"}}))
                   {:port 8080 :join? false}))

(defonce server (atom nil))

(defn start-server []
  (reset! server (run-jetty)))

(defn stop-server []
  (swap! server (fn [server]
                  (when server
                    (.stop server)))))

(defn -main []
  (run-jetty))

(comment 
  
  (stop-server)
  
  (do
    (stop-server)
    (start-server)))
