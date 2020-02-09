(ns noaa.core
  (:require [noaa.generation :as gen]
            [noaa.persistence.db :as db])
  (:gen-class))


(defn identify-noaas []
  (try
    (info "Beginning NOAA identification")
    (doseq [{:leads/keys [lead_id]} (db/find-leads-needing-noaas)]
      (info "NOAA required for lead:" lead_id)
      (let [{:leads_noaas/keys [id]} (db/create-noaa-record lead_id)]
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
        (db/update-noaa-text id email noaa-text template-type)
        (info "NOAA updated")))
    (catch Exception e
      (error "Exception encountered while generating NOAAS:"
             (with-out-str (clojure.stacktrace/print-stack-trace e))))
    (finally
      (info "NOAA generation complete"))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


(comment
  (identify-noaas)
  (generate-noaas)
  )
