(ns noaa.visine
  (:require [cheshire.core :refer [parse-string]]))


;; General protocol allowing noaa processes to request
;; a previously cached Clarity report, implementable in
;; using various differing approaches.

(defprotocol Visine
  (-service-details [this])
  (-get-cached-clarity-report [this ssn]))



(defonce mock-visine-data (delay (-> "sample_data/mock_visine_report.json"
                                     clojure.java.io/resource
                                     slurp
                                     (parse-string true))))


(defn mock-visine-service
  "In-process simulator of the Visine service.  Checks
   the ending digit of the supplied SSN and either returns
   a static Visine report (see mock visine data
   above) when the end digit is even or an empty map
  (pretending to be a case where no Clarity pull occurred)
   when the ending digit is odd."
  []
  (reify Visine
    (-service-details [_]
      "MockVisineService")
    (-get-cached-clarity-report [_ ssn]
      (if (re-find #"[02468]$" ssn)
        @mock-visine-data
        {}))))



(defn make-visine
  "Returns an instance of the Visine access service
   implementation configured as the default one by the
   noaa system.  At present, only the mock service exists"
  []
  (mock-visine-service))


;; Public interface for retrieving Clarity reports as
;; part of noaa generation.

(defn service-details
  "Returns details about the active Visine service."
  []
  (-service-details (make-visine)))


(defn get-cached-clarity-report
  "Given a social security number, retrieves the Clarity
   report associated with that ssn (if any exists) or
   whatever facsimilie of one the configured default
   Clarity service chooses to return (for testing data"
  [ssn]
  (-get-cached-clarity-report (make-visine) ssn))
