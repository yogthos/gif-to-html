(ns gif-to-html.convert
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]
            [mikera.image.core :refer [scale-image]])
  (:import javax.imageio.ImageIO
           java.io.File
           [java.awt.image BufferedImage]
           [java.awt Color RenderingHints]
           javax.swing.ImageIcon))

(def ascii [\# \A \@ \% \$ \+ \= \* \: \, \. \space])
(def max-items (dec (count ascii)))

(defn colors [img x y]
  (let [c (Color. (.getRGB img x y))]
    [(.getRed c) (.getGreen c) (.getBlue c)]))

(defn ascii-color [img y x]
  (let [[r g b :as rgb] (colors img x y)
        max-color (apply max rgb)
        idx (if (zero? max-color) max-items (int (+ (* max-items (/ max-color 255)) 0.5)))]
    (nth ascii (max idx 0))))

(defn to-ascii [img]
  (let [width  (.getWidth img)
        height (.getHeight img)
        sb     (StringBuilder.)]
    (dotimes [y height]
      (dotimes [x width]
        (.append sb (ascii-color img y x)))
      (.append sb "\n"))
    (.toString sb)
    #_(apply concat
           (for [y (range height)]
      (conj (mapv (partial ascii-color img y) (range width)) [:br])))))


(defn scale [x y]
  [100 (int (* (/ 100 x) y))])

(defn gif->html [input]
  (let [rdr  (.next (ImageIO/getImageReadersByFormatName "gif"))
        ciis (ImageIO/createImageInputStream input)]
    (.setInput rdr ciis false)
    (let [frame-count (.getNumImages rdr true)
          w           (.getWidth rdr 0)
          h           (.getHeight rdr 0)
          [w h]       (if (> w h) (scale w h) (scale h w))]
      {:data
       (html5
         [:div.animation
          (map (fn [i]
                  [:pre
                   {:id (str "frame-" i)
                    :style "font-size:6pt; letter-spacing:1px; line-height:6pt; font-weight:bold; display: none;font-family:monospace;"}
                   (to-ascii (scale-image (.read rdr i) w h))])
               (range frame-count))])
       :frames frame-count})))
