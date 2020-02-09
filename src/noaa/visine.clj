(ns noaa.visine
  (:require [cheshire.core :refer [parse-string]]))

(defprotocol Visine
  (_get-cached-clarity-report [this ssn]))



(defonce mock-visine-data (delay (-> "sample_data/mock_visine_report.json"
                                     clojure.java.io/resource
                                     slurp
                                     (parse-string true))))


(defn mock-visine-service []
  (reify Visine
    (_get-cached-clarity-report [_ _]
      (if (= (rand-int 2) 0)
        @mock-visine-data
        {}))))



(defn make-visine []
  (mock-visine-service))


(defn get-cached-clarity-report [ssn]
  (_get-cached-clarity-report (make-visine) ssn))


(comment
  (get-cached-clarity-report "123")
  
  )













