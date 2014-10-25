(defproject gif-to-html "1.0"
  :description "GIF to ASCII animation converter"
  :url "https://github.com/yogthos/gif-to-html"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [lib-noir "0.9.4"]
                 [compojure "1.1.6"]
                 [ring-server "0.3.1"]
                 [selmer "0.7.2"]
                 [clj-http "1.0.0"]
                 [net.mikera/imagez "0.4.1"]
                 [bk/ring-gzip "0.1.1"]]

  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler gif-to-html.handler/app}
  :profiles
  {:uberjar {:aot :all}
   :production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring/ring-devel "1.3.1"]]}}
  :min-lein-version "2.0.0")
