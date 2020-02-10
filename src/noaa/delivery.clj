(ns noaa.delivery
  (:require [environ.core :refer [env]]
            [noaa.delivery.filesystem :as f]
            [noaa.delivery.smtp :as smtp]))

(defprotocol Delivery
  (_deliver-noaa [this noaa]))


(defn filesystem-delivery-service []
  (reify Delivery
    (_deliver-noaa [_ noaa]
      (f/_deliver-noaa noaa))))


(defn smtp-delivery-service []
  (reify Delivery
    (_deliver-noaa [_ noaa]
      (smtp/_deliver-noaa noaa))))


(defn bitbucket-delivery-service []
  (reify Delivery
    (_deliver-noaa [_ noaa]
      (assoc noaa :delivery-status "Delivered to /dev/null"))))


(defn make-delivery-service []
  (case (env :noaa-delivery-service)
    "smtp" (smtp-delivery-service)
    "filesystem" (filesystem-delivery-service)
    (bitbucket-delivery-service)))


(defn deliver-noaa [noaa]
  (_deliver-noaa (make-delivery-service) noaa))










