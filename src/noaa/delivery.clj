(ns noaa.delivery
  (:require [environ.core :refer [env]]
            [noaa.delivery.filesystem :as f]
            [noaa.delivery.smtp :as smtp]))


(defprotocol Delivery
  "Defines a protocol for transporting NOAA content
   to its intended recipient."
  (-deliver-noaa [this noaa]))


(defn filesystem-delivery-service
  "'Delivers' a noaa message to a file on the
   local filesystem.  Useful for being able to
   inspect content generated for noaa messaging"
  []
  (reify Delivery
    (-deliver-noaa [_ noaa]
      (f/-deliver-noaa noaa))))


(defn smtp-delivery-service
  "Transmits a noaa message to a recipient via smtp."
  []
  (reify Delivery
    (-deliver-noaa [_ noaa]
      (smtp/-deliver-noaa noaa))))


(defn bitbucket-delivery-service
  "Convenience no-op delivery service which does
   nothing but doesn't interrupt noaa delivery processing"
  []
  (reify Delivery
    (-deliver-noaa [_ noaa]
      (assoc noaa :delivery-status "Delivered to /dev/null"))))


(defn echo-delivery-service
  "Convenience delivery service which just echoes the
   NOAA message to stdout."
  []
  (reify Delivery
    (-deliver-noaa [_ {:noaas/keys [noaa_text] :as noaa}]
      (assoc noaa :delivery-status noaa_text))))


(defn make-delivery-service
  "Determines the delivery service implementation to use by
   inspecting the NOAA_DELIVERY_SERVICE environment var."
  []
  (case (env :noaa-delivery-service)
    "smtp" (smtp-delivery-service)
    "filesystem" (filesystem-delivery-service)
    "echo" (echo-delivery-service)
    (bitbucket-delivery-service)))


;; Delivery functions for public (out of ns) usage.

(defn deliver-noaa
  "Given a noaa, attempts to deliver its contents to its
   intended recipient using whatever means are provided by
   the delivery system configured as the default one."
  [noaa]
  (-deliver-noaa (make-delivery-service) noaa))










