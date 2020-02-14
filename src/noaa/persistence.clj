(ns noaa.persistence
  (:require [noaa.persistence.db :as db]))


(defprotocol NOAAData
  "A protocol to manage finders/updaters of NOAA data."
  (-service-details [this])
  (-create-noaa-record! [this lead-id])
  (-update-noaa-text! [this noaa-id send-to
                      noaa-text template-type
                      noaa-data])
  (-update-noaa-as-sent! [this noaa-id])
  (-find-leads-needing-noaas [this])
  (-find-noaas-needing-generation [this])
  (-find-noaas-needing-sending [this]))



(defn db-noaa-data
  "Reifies NOAAData using a relational db as a datastore."
  []
  (reify NOAAData
    (-service-details [_]
      (db/-service-details))
    (-create-noaa-record! [_ lead-id]
      (db/-create-noaa-record! lead-id))
    (-update-noaa-text! [_ noaa-id send-to
                        noaa-text template-type
                        noaa-data]
      (db/-update-noaa-text! noaa-id send-to noaa-text
                             template-type noaa-data ))
    (-update-noaa-as-sent! [_ noaa-id]
      (db/-update-noaa-as-sent! noaa-id))
    (-find-leads-needing-noaas [_]
      (db/-find-leads-needing-noaas))
    (-find-noaas-needing-generation [_]
      (db/-find-noaas-needing-generation))
    (-find-noaas-needing-sending [_]
      (db/-find-noaas-needing-sending))))


(defn make-noaa-data-service
  "Returns an reified NOAA data service instance of the type
   specified by the system config.  At present, only the db
   service exists."
  []
  (db-noaa-data))


;; Public interface for interacting with NOAA data

(defn service-details []
  (-service-details (make-noaa-data-service)))


(defn create-noaa-record!
  "Given a lead ID, creates a NOAA record"
  [lead-id]
  (-create-noaa-record! (make-noaa-data-service) lead-id))


(defn update-noaa-text!
  "Updates a NOAA record with data produced by a message
   generation pass on the record."
  [noaa-id send-to noaa-text template-type noaa-data]
  (-update-noaa-text! (make-noaa-data-service) noaa-id send-to noaa-text
                     template-type noaa-data))


(defn update-noaa-as-sent!
  "Marks the NOAA as having been successfully sent during
   a delivery processing."
  [noaa-id]
  (-update-noaa-as-sent! (make-noaa-data-service) noaa-id))


(defn find-leads-needing-noaas
  "Find all leads requiring a NOAA be generated."
  []
  (-find-leads-needing-noaas (make-noaa-data-service)))


(defn find-noaas-needing-generation
  "Find all NOAAS which have not yet had message generation
   performed on them."
  []
  (-find-noaas-needing-generation (make-noaa-data-service)))


(defn find-noaas-needing-sending
  "Find all NOAAs ready for delivery (message text generated.)"
  []
  (-find-noaas-needing-sending (make-noaa-data-service)))










