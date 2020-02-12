(ns noaa.core
  (:require [cheshire.core :refer [generate-string]]
            [cheshire.generate :refer [add-encoder]]
            [noaa.generation :as gen]
            [noaa.persistence.db :as db]
            [noaa.delivery :as delivery]
            [taoensso.timbre :refer [info debug error warn]])
  (:gen-class))


;; Cheshire chokes on the newer date types;
;; provide an encoder to ease its pain 
(add-encoder java.time.LocalDate
             (fn [c json-writer]
               (.writeString json-writer (.toString c))))


(defn identify-noaas
  "Scans the specified leads database looking for leads
   which have been Clarity failed but have not previously
   generated a noaa.  For each matching lead, a noaa record
   is created, which will be processed in the next step of
   noaa handling, generate-noaas."
  []
  (try
    (info "Beginning NOAA identification")
    (doseq [{:leads_noaas/keys [lead_id]} (db/find-leads-needing-noaas)]
      (info "NOAA required for lead:" lead_id)
      (let [{:noaas/keys [id]} (db/create-noaa-record! lead_id)]
        (info "Created NOAA ID" id "for lead" lead_id)))
    (catch Exception e
      (error "Exception encountered while identifying NOAAS:"
             (with-out-str (clojure.stacktrace/print-stack-trace e))))
    (finally
      (info "NOAA identification complete."))))


(defn generate-noaas
  "Finds all noaas which have not be marked as having been processed by
   a prior generate-noaas run.  Each of these has its noaa message
   created by the noaa templating system, the generated message is then
   stored in the noaa for use in the final processing step, delivery."
  []
  (try
    (info "Beginning NOAA generation")
    (doseq [{:noaas/keys [id] :as noaa}
            (db/find-noaas-needing-generation)]
      (try
        (info "NOAA" id "needs generation; preparing text" )
        (let [{:keys [noaa-text]
               {:keys [email]} :request
               {:keys [template-type]} :meta
               :as generated-noaa}
              (gen/process-noaa-generation {:raw-noaa noaa})]
          (info "NOAA text generated for" id ";saving text to db.")
          (db/update-noaa-text! id email noaa-text
                                template-type
                                (generate-string
                                 (dissoc generated-noaa :noaa-text)))
          (info "NOAA updated"))
        (catch Exception e
          (warn e
                "Exception while attempting to generate NOAA" id
                "(" (.getMessage e) ")"
                "; generation skipped..."))))
    (catch Exception e
      (error "General exception encountered while generating NOAAS:"
             (with-out-str (clojure.stacktrace/print-stack-trace e))))
    (finally
      (info "NOAA generation complete"))))


(defn send-noaas
  "The final step of noaa processing.  All noaas generated but not successfully
   delivered to recipients as part of a prior delivery job have their messages
   delivered (typically by email) to their intended recipient."
  []
  (try
    (info "Beginning NOAA delivery")
    (doseq [{:noaas/keys [id] :as noaa} (db/find-noaas-needing-sending)]
      (info "NOAA" id "needs to be sent." )
      (try
        (let [{:keys [delivery-status]} (delivery/deliver-noaa noaa)]
          (info "NOAA" id " sent; delivery status:" delivery-status)
          (db/update-noaa-as-sent! id)
          (info "NOAA status updated"))
        (catch Exception e
          (warn "Problem while sending NOAA" id "("
                (.getMessage e)
                ");"
                "will attempt to re-send as part of the next delivery pass."))))
    (catch Exception e
      (error "Exception encountered while sending NOAAS:"
             (with-out-str (clojure.stacktrace/print-stack-trace e))))
    (finally
      (info "NOAA delivery complete"))))


;; Demo functions.

(defn gen-some-leads
  "Generate some fake lead data for testing/demo purposes"
  []
  (info "Generating 10 fake leads...")
  (dotimes [n 10] (db/create-fake-lead))
  (info "Generation complete."))


(defn demo-noaas
  "Generate some data then run the entire processing
   cycle."
  []
  (gen-some-leads)
  (identify-noaas)
  (generate-noaas)
  (send-noaas))


(defn -main
  [action & rest]  
  (case action
    "identify-noaas" (identify-noaas)
    "generate-noaas" (generate-noaas)
    "send-noaas" (send-noaas)
    "gen-some-leads" (gen-some-leads)
    "demo-noaas" (demo-noaas)
    (error "Unknown action:" action)))
  


(comment
  (identify-noaas)
  (generate-noaas)
  (send-noaas)
  )









