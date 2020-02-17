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
          (str (offset-date-time))))


(defn -service-details
  "Prints the file path used as the directory where
   NOAA messages are being written as files."
  []
  (format "FilesystemDelivery: default path %s"
          (noaa-file-path-prefix)))


(defn -deliver-noaa
  "Writes the NOAA message to a file, in directory
  (noaa-file-path-prefix) using the noaa ID and
  current datetime to form a unique filename."
  [{:noaas/keys [noaa_text] :as noaa}]
  (spit (noaa-file-path noaa) noaa_text))
