(ns noaa.persistence.db
  (:require [environ.core :refer [env]]
            [hikari-cp.core :refer :all]
            [java-time :refer [local-date offset-date-time]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql])
  (:import (java.util UUID)))


(def ds-options {:pool-name     "lz-pool"
                 :adapter       "postgresql"
                 :server-name   (env :leads-database-hostname)
                 :database-name (env :leads-database-name)})


(defonce ds (delay (make-datasource ds-options)))


(defn test-ds []
  (let [rows (jdbc/execute! @ds ["select 1 as test"])]
    (println rows)))


(defn create-dummy-lead []
  (sql/insert! @ds :leads {:id (UUID/randomUUID)
                           :status 623}))


(defn find-lead [id]
  (sql/get-by-id @ds :leads id))


(defn find-noaa [id]
  (sql/get-by-id @ds :leads_noaas id))



(defn all-leads []
  (jdbc/execute! @ds ["select * from leads"]))


(defn create-fake-lead []
  (let [lead (fake-lead)
        id (UUID/randomUUID)]
    (jdbc/execute! @ds
                   ["insert into leads
                    (id,request,status)
                    values (?, ?::json,623)"
                    id
                    (:request lead)])))


(defn create-noaa-record [lead-id]
  (sql/insert! @ds :leads_noaas
               {:id (UUID/randomUUID)
                :lead_id lead-id
                :noaa_identified_at (offset-date-time)}))


(defn update-noaa-text [noaa-id send-to noaa-text template-type]
  (sql/update! @ds :leads_noaas
               {:noaa_text noaa-text
                :noaa_template_type template-type
                :noaa_destination_email send-to
                :noaa_generated_at (offset-date-time)
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
  (sql/query @ds ["select l.id as lead_id 
                   from leads l left join leads_noaas n on l.id = n.lead_id
                   where n.noaa_identified_at is null
                     and l.status = 623
                     and l.created_date >= ?"
                  (local-date
                   "yyyy-MM-dd"
                   (env :leads-noaa-cutoff-date))]))


(defn find-noaas-needing-generation
  "Any NOAA with a null noaa_generated_at date has not
   had its message generated successfully as of yet."
  []
  (sql/query @ds ["select n.*,
                          l.*
                  from leads_noaas n
                  inner join leads l on l.id = n.lead_id
                  where n.noaa_generated_at is null"]))


(comment
  (test-ds)
  (create-dummy-lead)
  (create-dummy-lead)
  (let [id (-> (all-leads)
               first
               :leads/id)]
    (create-noaa-record id)
    )
  (find-leads-needing-noaas)
  (find-noaas-needing-generation)
  (create-fake-lead)
  (-> (all-leads)
      first
      :leads/request
      .getValue)
  )
