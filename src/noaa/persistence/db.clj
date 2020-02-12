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
                 :server-name   (env :leads-database-hostname)
                 :database-name (env :leads-database-name)})


(defonce ds (delay (make-datasource ds-options)))


;; Convenience finders for leads and noaas.
(defn find-lead [id]
  (sql/get-by-id @ds :leads id))


(defn find-noaa [id]
  (sql/get-by-id @ds :noaas id))


(defn create-fake-lead
  "Convenience method to generate a fake
   Clarity denial lead.  DO NOT invoke
   this function if your database connection
   is to any sort of live db."
  []
  (let [lead (fake-lead)
        id (UUID/randomUUID)]
    (jdbc/execute! @ds
                   ["insert into leads
                    (id,request,status)
                    values (?, ?::json,623)"
                    id
                    (:request lead)])))


(defn create-noaa-record!
  "Given a lead id, creates a new uninitialized
   NOAA in the attached db.  This NOAA will be
   picked up by the next generate-lead process and
   have its notice message generated."
  [lead-id]
  (sql/insert! @ds :noaas
               {:id (UUID/randomUUID)
                :lead_id lead-id
                :noaa_identified_at (offset-date-time)}))


(defn update-noaa-text!
  "Called as part of the generate-noaas processing,
   given a noaa ID, updates the NOAA to include
   message text, the sending destination and template
   type used to generate the message.  Also updates
   the noaa_generated_at attribute, which means that
   the noaa will be included in the next send-noaas 
   processing run."
  [noaa-id send-to noaa-text template-type]
  (sql/update! @ds :noaas
               {:noaa_text noaa-text
                :noaa_template_type template-type
                :noaa_destination_email send-to
                :noaa_generated_at (offset-date-time)
                :updated_at (offset-date-time)}
               {:id noaa-id}))


(defn update-noaa-as-sent!
  "Called as part of send-noaas processing, updates
   the noaa with some audit information relating to
   delivery of the noaa message.  After this update,
   the noaa processing is considered complete."
  [noaa-id]
  (sql/update! @ds :noaas
               {:noaa_transmitted_at (offset-date-time)
                :updated_at (offset-date-time)}
               {:id noaa-id}))


(defn find-leads-needing-noaas
  "A lead requires a NOAA if the following conditions have been met:
   - status = 623 (Clarity rejected)
   - No NOAA has been previously generated for the lead.

   We utilize SQL join semantics to check the second condition: by
   left joining leads back to the noaas table, any lead which hasn't
   previously generated a NOAA will present an empty noaa row in the
   resulting dataset.  By checking a record's noaa_identified_at column for
   nil, we can determine that a NOAA was never generated for the 
   corresponding lead and that we should generate one.

   Note that a much easier way to do this would be to add some
   sentinal attribute in the actual lead table and check it.  For the
   moment we are trying to avoid mucking around in the LZ core schema
   so we take this approach.  Definitely a IMPROVEME opportunity for
   the future."
  []
  (sql/query @ds ["select lead_id
                   from leads_noaas
                   where noaa_identified_at is null
                     and lead_status = 623
                     and lead_created_date >= ?"
                  (local-date
                   "yyyy-MM-dd"
                   (env :leads-noaa-cutoff-date))]))



(defn find-noaas-needing-generation
  "Retrieves any NOAA with a null noaa_generated_at date (has not
   had its message generated successfully as of yet.)"
  []
  (sql/query @ds ["select n.*,
                          l.*
                  from  noaas n
                  inner join leads l on l.id = n.lead_id
                  where n.noaa_generated_at is null"]))


(defn find-noaas-needing-sending
  "Retrieves any noaa which has completed generation processing but
   has not yet been delivered to its intended recipient."
  []
  (sql/query @ds ["select * from noaas
                   where noaa_transmitted_at is null
                     and noaa_generated_at is not null"]))


(comment
  (find-leads-needing-noaas)
  (find-noaas-needing-generation)
  (find-noaas-needing-sending)
  )









