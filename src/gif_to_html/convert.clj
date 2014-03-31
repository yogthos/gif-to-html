(ns gif-to-html.convert
  (:require [hiccup.page :refer [html5]]
            [mikera.image.core :refer [scale-image]])
  (:import [javax.imageio ImageReader ImageIO]
           [java.awt.image BufferedImage]
           java.awt.Color))

(def ascii [\# \@ \O \% \$ \i \o \c \* \; \: \+ \! \^ \' \- \. \space])
(def max-items (dec (count ascii)))

(defn ascii-color [^BufferedImage img ^Integer y ^Integer x]
  (let [pixel (.getRGB img x y)        
        r (bit-and (bit-shift-right pixel 16) 0x000000FF)
        g (bit-and (bit-shift-right pixel 8) 0x000000FF)
        b (bit-and pixel 0x000000FF)
        max-color (int (Math/sqrt (+ (* r r 0.241) (* g g 0.691) (* b b 0.068))))
        idx (if (zero? max-color) max-items (int (* max-items (/ max-color 255))))]
    (nth ascii (max idx 0))))

(defn to-ascii [^BufferedImage img ^Integer size]
  (let [width  (.getWidth img)
        height (.getHeight img)
        sb     (StringBuilder. size)]
    (dotimes [y height]
      (dotimes [x width]
        (.append sb (ascii-color img y x)))
      (.append sb \newline))
    (.toString sb)))

(defn scale [a b]
  (int (* (/ 100 a) b)))

(defn delay [rdr]
  (try
    (let [image-meta  (.getImageMetadata rdr 0)
          format-name (.getNativeMetadataFormatName image-meta)
          root        (.getAsTree image-meta format-name)]
      (as-> (range (.getLength root)) x
            (map #(.item root %) x)
            (filter #(= 0 (.compareToIgnoreCase (.getNodeName %) "GraphicControlExtension")) x)
            (first x)
            (.getAttribute x "delayTime")
            (Integer/parseInt x)))
    (catch Exception _ 0)))

(defn gif->html [input]
  (let [rdr  ^ImageReader (.next (ImageIO/getImageReadersByFormatName "gif"))
        ciis (ImageIO/createImageInputStream input)]
    (.setInput rdr ciis false)    
    (let [frame-count (.getNumImages rdr true)
          frame-delay (delay rdr)
          w           (.getWidth rdr 0)
          h           (.getHeight rdr 0)
          [w h]       (cond
                       (and (< w 150) (< h 150)) [w h]
                       (> w h) [100 (scale w h)]
                       :else [(scale h w) 100])]
      {:data
       (html5
         [:div.animation
          (map (fn [i]
                  [:pre
                   {:id (str "frame-" i)
                    :style "font-size:5pt;line-height:5pt;letter-spacing:1px;font-weight:bold;display:none;font-family:monospace;"}
                   (to-ascii (scale-image (.read ^ImageReader rdr i) w h) (+ h (* h w)))])
               (range frame-count))])
       :delay  (if (pos? frame-delay) frame-delay 10)
       :frames frame-count})))
