(ns noaa.core
  (:require [noaa.generation :as gen]
            [noaa.persistence.db :as db]
            [noaa.delivery :as delivery]
            [taoensso.timbre :refer [info debug error warn]])
  (:gen-class))


(defn identify-noaas []
  (try
    (info "Beginning NOAA identification")
    (doseq [{:leads/keys [lead_id]} (db/find-leads-needing-noaas)]
      (info "NOAA required for lead:" lead_id)
      (let [{:leads_noaas/keys [id]} (db/create-noaa-record! lead_id)]
        (info "Created NOAA ID" id "for lead" lead_id)))
    (catch Exception e
      (error "Exception encountered while identifying NOAAS:"
             (with-out-str (clojure.stacktrace/print-stack-trace e))))
    (finally
      (info "NOAA identification complete."))))


(defn generate-noaas []
  (try
    (info "Beginning NOAA generation")
    (doseq [{:leads_noaas/keys [id] :as noaa} (db/find-noaas-needing-generation)]
      (info "NOAA" id "needs generation; preparing text" )
      (let [{:keys [noaa-text]
             {:keys [email]} :request
             {:keys [template-type]} :meta
             :as generated-noaa}
            (gen/process-noaa-generation {:raw-noaa noaa})]
        (info "NOAA text generated for" id ";saving text to db.")
        (db/update-noaa-text! id email noaa-text template-type)
        (info "NOAA updated")))
    (catch Exception e
      (error "Exception encountered while generating NOAAS:"
             (with-out-str (clojure.stacktrace/print-stack-trace e))))
    (finally
      (info "NOAA generation complete"))))


(defn send-noaas []
  (try
    (info "Beginning NOAA delivery")
    (doseq [{:leads_noaas/keys [id] :as noaa} (db/find-noaas-needing-sending)]
      (info "NOAA" id "needs to be sent." )
      (try
        (let [{:keys [delivery-status]} (delivery/deliver-noaa noaa)]
          (info "NOAA" id " sent; delivery status:" delivery-status)
          (db/update-noaa-as-sent! id)
          (info "NOAA status updated"))
        (catch Exception e
          (warn "Problem while sending NOAA" id ":" e))))
    (catch Exception e
      (error "Exception encountered while sending NOAAS:"
             (with-out-str (clojure.stacktrace/print-stack-trace e))))
    (finally
      (info "NOAA delivery complete"))))


(defn -main
  [action & rest]  
  (case action
    "identify-noaas" (identify-noaas)
    "generate-noaas" (generate-noaas)
    "send-noaas" (send-noaas)
    "gen-some-leads" (do
                       (info "Generating 10 fake leads...")
                       (dotimes [n 10] (db/create-fake-lead))
                       (info "Generation complete."))
    (error "Unknown action:" action)))
  


(comment
  (identify-noaas)
  (generate-noaas)
  (send-noaas)
  )









