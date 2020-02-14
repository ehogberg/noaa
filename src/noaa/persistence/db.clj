(ns noaa.persistence.db
  (:require [environ.core :refer [env]]
            [hikari-cp.core :refer :all]
            [java-time :refer [local-date offset-date-time]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [noaa.util.fake :refer [fake-lead]])
  (:import (java.util UUID)))


(def ds-options {:pool-name     "lz-pool"
                 :adapter       "postgresql"
                 :server-name   (env :noaas-database-hostname)
                 :database-name (env :noaas-database-name)})


(defonce ds (delay (make-datasource ds-options)))



;; Convenience finders for leads and noaas.
(defn- find-lead [id]
  (sql/get-by-id @ds :leads id))


(defn- find-noaa [id]
  (sql/get-by-id @ds :noaas id))


(defn create-fake-lead
  "Convenience method to generate a fake
   Clarity denial lead.  DO NOT invoke
   this function if your database connection
   is to any sort of live db."
  []
  (let [{:keys [request ssn]} (fake-lead)
        id (UUID/randomUUID)]
    (jdbc/execute! @ds
                   ["insert into leads
                    (id,request,status,version,ssn)
                    values (?, ?::json,623,'5',?)"
                    id request ssn])))


;; NOAAData protocol implementation...

(defn -service-details []
  (format "DB-NOAAData: //%s/%s"
          (env :noaas-database-hostname)
          (env :noaas-database-name)))


(defn -create-noaa-record!
  "Given a lead id, creates a new uninitialized
   NOAA in the attached db.  This NOAA will be
   picked up by the next generate-lead process and
   have its notice message generated."
  [lead-id]
  (sql/insert! @ds :noaas
               {:id (UUID/randomUUID)
                :lead_id lead-id
                :noaa_identified_at (offset-date-time)}))


(defn -update-noaa-text!
  "Called as part of the generate-noaas processing,
   given a noaa ID, updates the NOAA to include
   message text, the sending destination and template
   type used to generate the message.  Also updates
   the noaa_generated_at attribute, which means that
   the noaa will be included in the next send-noaas 
   processing run."
  [noaa-id send-to noaa-text template-type noaa-data]
  (jdbc/execute! @ds
                 ["update noaas
                   set noaa_text = ?, noaa_template_type = ?,
                       noaa_destination_email = ?,
                       noaa_generated_at = ?,
                       updated_at = ?,
                       noaa_generation_data = ?::json
                   where id = ?"
                  noaa-text template-type
                  send-to (offset-date-time)
                  (offset-date-time) noaa-data
                  noaa-id]))


(defn -update-noaa-as-sent!
  "Called as part of send-noaas processing, updates
   the noaa with some audit information relating to
   delivery of the noaa message.  After this update,
   the noaa processing is considered complete."
  [noaa-id]
  (sql/update! @ds :noaas
               {:noaa_transmitted_at (offset-date-time)
                :updated_at (offset-date-time)}
               {:id noaa-id}))


(defn -find-leads-needing-noaas
  "A lead requires a NOAA if the following conditions have been met:
   - status = 623 (Clarity rejected)
   - No NOAA has been previously generated for the lead.

  We query the leads_noaas view created in the reporting db which
  joins leads and noaas across different schemas, rather than
  trying to manually join them ourselves which will likely run
  afoul of access security issues.

  TODO: Add a date window parameter to the where clause so we're
  not joining hundreds of thousands of older records needlessly.
  "
  []
  (sql/query @ds ["select lead_id
                   from leads_noaas
                   where noaa_identified_at is null
                     and lead_status = 623
                     and lead_created_date >= ?"
                  (local-date
                   "yyyy-MM-dd"
                   (env :leads-noaa-cutoff-date))]))



(defn -find-noaas-needing-generation
  "Retrieves any NOAA with a null noaa_generated_at date (has not
   had its message generated successfully as of yet.)"
  []
  (sql/query @ds ["select n.*,
                          l.*
                  from  noaas n
                  inner join leads l on l.id = n.lead_id
                  where n.noaa_generated_at is null"]))


(defn -find-noaas-needing-sending
  "Retrieves any noaa which has completed generation processing but
   has not yet been delivered to its intended recipient."
  []
  (sql/query @ds ["select * from noaas
                   where noaa_transmitted_at is null
                     and noaa_generated_at is not null"]))


(comment
  (-find-leads-needing-noaas)
  (-find-noaas-needing-generation)
  (-find-noaas-needing-sending)
  )









