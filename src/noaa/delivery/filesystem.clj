(ns noaa.delivery.filesystem
  (:require [environ.core :refer [env]]
            [java-time :refer [offset-date-time]]))


(defn noaa-file-path-prefix []
  (or (env :filesystem-delivery-path)
      "."))


(defn noaa-file-path [{:noaas/keys [id] :as noaa}]
  (format "%s/%s_%s.txt"
          (noaa-file-path-prefix)
          id
          (.toString (offset-date-time))))


(defn _deliver-noaa [{:noaas/keys [noaa_text] :as noaa}]
  (spit (noaa-file-path noaa) noaa_text))












