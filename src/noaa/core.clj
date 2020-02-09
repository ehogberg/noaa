(ns noaa.core
  (:require [noaa.persistence.db :as db]
            [taoensso.timbre :as timbre :refer [log info]])
  (:gen-class))


(defn identify-noaas []
  (info "Beginning NOAA identification")
  (doseq [{:leads/keys [lead_id]} (db/find-leads-needing-noaas)]
    (info "NOAA required for lead:" lead_id)
    (let [{:leads_noaas/keys [id]} (db/create-noaa-record lead_id)]
      (info "Generated NOAA ID" id "for lead" lead_id)))
  (info "NOAA identification complete."))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))






