(ns gif-to-html.convert
  (:require [mikera.image.core :refer [scale-image]])
  (:import [javax.imageio ImageReader ImageIO]
           [java.awt.image BufferedImage]))

(def ascii [\# \@ \O \% \$ \i \o \c \* \; \: \+ \! \^ \' \- \. \space])
(def max-items (dec (count ascii)))

(defn pixel->ascii [^Integer pixel]
  (let [r (bit-and (bit-shift-right pixel 16) 0x000000FF)
        g (bit-and (bit-shift-right pixel 8) 0x000000FF)
        b (bit-and pixel 0x000000FF)
        max-color (Math/sqrt (+ (* r r 0.241) (* g g 0.691) (* b b 0.068)))
        idx (if (zero? max-color) max-items (int (* max-items (/ max-color 255))))]
    (nth ascii (max idx 0))))

(defn to-ascii [^BufferedImage img ^Integer w ^Integer h]
  (let [^ints pixels (.. img getRaster getDataBuffer getData)
        sb (StringBuilder. (int (+ h (* h w))))]
    (dotimes [i (count pixels)]
      (.append sb (pixel->ascii (aget pixels i)))
      (when (zero? (mod i w)) (.append sb \newline)))
    (.toString sb)))

(defn scale [a b]
  (int (* (/ 150 a) b)))

(defn find-delay [rdr]
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
          frame-delay (find-delay rdr)
          w           (.getWidth rdr 0)
          h           (.getHeight rdr 0)
          [w h]       (cond
                       (and (< w 150) (< h 150)) [w h]
                       (> w h) [150 (scale w h)]
                       :else [(scale h w) 150])]
      {:frames
       (map (fn [i]
              [:pre
               {:id (str "frame-" i)
                :style "font-size:4pt;line-height:4pt;letter-spacing:1px;font-weight:bold;display:none;font-family:monospace;"}
               (to-ascii (scale-image (.read ^ImageReader rdr i) w h) w h)])
            (range frame-count))
       :delay  (* 10 (if (pos? frame-delay) frame-delay 10))
       :frame-count frame-count})))
