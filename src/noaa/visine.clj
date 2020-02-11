(ns noaa.visine
  (:require [cheshire.core :refer [parse-string]]))


;; General protocol allowing noaa processes to request
;; a previously cached Clarity report, implementable in
;; using various differing approaches.

(defprotocol Visine
  (_get-cached-clarity-report [this ssn]))



(defonce mock-visine-data (delay (-> "sample_data/mock_visine_report.json"
                                     clojure.java.io/resource
                                     slurp
                                     (parse-string true))))


(defn mock-visine-service
  "In-process simulator of the Visine service.  Flips a coin
   metaphorically speaking and, depending on the result,
   returns either a static Visine report (see mock visine data
   above) or an empty map (pretending to be a case where no
   Clarity pull occurred."
  []
  (reify Visine
    (_get-cached-clarity-report [_ _]
      (if (= (rand-int 2) 0)
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

(defn get-cached-clarity-report
  "Given a social security number, retrieves the Clarity
   report associated with that ssn (if any exists) or
   whatever facsimilie of one the configured default
   Clarity service chooses to return (for testing data"
  [ssn]
  (_get-cached-clarity-report (make-visine) ssn))












