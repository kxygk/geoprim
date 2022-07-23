(ns
    geoprim
  "This defines geographic primitives through protocols
  .. such as regions and points")

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;; POINTS
(defprotocol
    geopoint
  "Operations that can be done on a Geographic Point"
  (as-eassou
    [point]
    "Return point as a [EAST, SOUTH] pair.. for display coords")
  (as-latlon
    [point]
    "Return poin as a [LATITUDE, LONGITUDE] pair"))

(defrecord
    eassou-point ;; EAST/SOUTH coordinate system point
    [^double eas ;; EAST
     ^double sou];; SOUTH
  geopoint
  (as-eassou
    [_]
    [eas
     sou])
  (as-latlon
    [_]
    [(-
       90.0
       sou)
     (+
       -180.0
       eas)]))

(defn point ;; DEPRECATED
  [^double lat
   ^double lon]
  (->eassou-point
    (+
      lon
      180)
    (-
      90
      lat)))

(defn point-latlon
  [^double lat
   ^double lon]
  (->eassou-point
    (+
      lon
      180)
    (-
      90
      lat)))

(defn point-eassou
  [^double eas
   ^double sou]
  (->eassou-point
    eas
    sou))
#_
(def
  taipei
  (point
    25
    121.5)) 


;; REGION
(defprotocol
    rect-region
  "A geographic region"
  (dimension
    [x]
    "Return the [width height] of a region in degrees")
  (point-to-eassou
    [x
     point]
    "Return the POINT in South/East coords relative to North/East corner")
  (four-corners
    [x]
    "Returns the four corners counter-clockwise from North/West"))
;; While GeoJSON doesn't seem to have a winding direction
;; https://geojson.org/geojson-spec#id4
;; The underlying JTS library does

(defrecord
    nwse-region
    [norwes
     soueas]
  rect-region
  (dimension
    [_]
    (let
        [[^double start-eas
          ^double start-sou] (as-eassou
                               norwes)
         [^double ended-eas
          ^double ended-sou] (as-eassou
                               soueas)]
      [(-
         ended-eas
         start-eas)
       (-
         ended-sou
         start-sou)]))
  (point-to-eassou
    [_
     point]
    (let [[^double corner-eas
           ^double corner-sou] (as-eassou
                                 norwes)
          [^double point-eas
           ^double point-sou]  (as-eassou
                                 point)]
      [(-
         point-eas
         corner-eas)
       (-
         point-sou
         corner-sou)]))
  (four-corners
    [_]
    (let
        [[start-lat
          start-lon] (as-latlon
                       norwes)
         [ended-lat
          ended-lon] (as-latlon
                       soueas)]
      [(point
         start-lat
         start-lon)
       (point
         start-lat
         ended-lon)
       (point
         ended-lat
         ended-lon)
       (point
         ended-lat
         start-lon)])))

(defn region
  "A North/East and South/West pair of points define a region"
  [norwes
   soueas]
  (->nwse-region
    norwes
    soueas))

;; Region Around Taiwan and Chinese coast
#_
(def
  minnan-region
  (region
    (point
      26.23
      116.47)
    (point
      21.7
      125)))

#_
(dimension
  minnan-region)
;; => [8.529999999999973 4.530000000000001]

#_
(point-to-eassou
  minnan-region
  (point
    25
    120))
;; => [3.5299999999999727 1.230000000000004]

#_
(four-corners
  minnan-region)
;; => [[116.47000000000003 26.230000000000004]
;;     [125.0 26.230000000000004]
;;     [125.0 21.700000000000003]
;;     [116.47000000000003 21.700000000000003]]
