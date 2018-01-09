(ns wonderland.core
  (:require [voronoi-diagram.core :as voronoi]))

(def max-coord 500)
(def N 4)

; Convenience extractors for points of the form [x y]
(defn x [point] (double (nth point 0)))
(defn y [point] (double (nth point 1)))

; Prefix for SVG file
(defn svg-prefix [width height]
  (let [prefix-string "<svg xmlns=\"http://www.w3.org/2000/svg\"
  xmlns:xlink=\"http://www.w3.org/1999/xlink\"
  width=\"%s\" height=\"%s\">"]
    (format prefix-string width height)
  )
)

; Suffix for SVG file
(def svg-suffix "</svg>")

; Draw the body of a point for SVG output
; Expects a coord of form [x y]
(defn point [coord]
  (let [
    radius 1
    ellipse-string "<ellipse
      cx=\"%.2f\"
      cy=\"%.2f\"
      rx=\"%d\"
      ry=\"%d\"
      stroke=\"#fc8d62\"
      stroke-width=\"4\"
    />"]
    (format ellipse-string (x coord) (y coord) radius radius))
)

(defn line [p1 p2]
  (let [
    line-string "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\"
      stroke-width=\"2\" stroke=\"black\"/>"
      x1 (x p1)
      y1 (y p1)
      x2 (x p2)
      y2 (x p2)
      ]
      (format line-string x1 y1 x2 y2)
      )
  )

(defn polygon [points]
  (apply str (for [i (range (- (count points) 1))]
    (let [p1 (nth points i)
          p2 (nth points (+ 1 i))]
          (line p1 p2)
          ))))

; DONT USE ME
; This is the "right" way to make a polygon but sadly useless for our purposes.
(defn polygon-by-polygon-svg [points]
  (let [
    polygon-string "<polygon fill=\"none\" stroke=\"black\" stroke-width=\"2\" points=\"%s\"/>"
    points-string (apply str (map (fn [p] (format "%.2f,%.2f " (x p) (y p))) points))
    ]
    (format polygon-string points-string))
  )

(defn edgeline [edge]
  (line (nth edge 0) (nth edge 1)))

(defn puzzlepath [point1 point2]
  (let [
    x1 (x point1)
    y1 (y point1)
    x2 (x point2)
    y2 (y point2)
    dx (- x2 x1)
    dy (- y2 y1)
    yscale (- 1 (* 2 (rand-int 2))) ; Until I find a non-destructive flip transform, this will have to wait.
    line-length (Math/sqrt (+ (* dx dx) (* dy dy)))
    angle (* (/ 180 Math/PI) (Math/atan2 dy dx))
    pathid (format "path-%d-%d-%d-%d" x1 y1 x2 y2)
    path-string "<path 
        d=\"M0,21 C30,28 45,28 45,21 C45,17 34,16 35,11 C36,6 42.5,1 50,1 C57.5,1 64,6 65,11 C66,16 55,20 55,21 C55,24 70,24 100,21\"
        id=\"%s\"
        stroke=\"#979797\"
        fill=\"none\"
        transform=\"translate(%d %d) rotate(%f 0 21) scale(%f 1)\">
      </path>"
    ]
    (format path-string pathid x1 (- y1 21) angle (/ line-length 100)))
)

; Sets up coordinates for puzzle piece anchor points
(def coords
  (vec (for [x (range N) y (range N)]
    (let [xcoord (+ 50 (* 100 x))
          ycoord (+ 50 (* 100 y))
          jitter 0
          xjitter (rand-int jitter)
          yjitter (rand-int jitter)
      ] (vec (list (+ xcoord xjitter) (+ ycoord yjitter)))))))

; Create an SVG, suitable for file output.
(defn svg [body]
  (println (svg-prefix max-coord max-coord))
  (println (apply str body))
  (println svg-suffix))

(defn -main
  []
  (let [
    {:keys [points edges cells]} (voronoi/diagram coords)
    edgelines (map edgeline edges)
    cell-lines (map polygon-by-polygon-svg cells)
    pointstrings (map point coords)
    svgbody (concat cell-lines pointstrings)]
    (svg svgbody)
  ))
