(ns noaa.persistence.db
  (:require [hikari-cp.core :refer :all]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql])
  (:import (java.util UUID)))


(def ds-options {:pool-name     "lz-pool"
                 :adapter       "postgresql"
                 :server-name   (System/getenv "LEADS_DATABASE_HOSTNAME")
                 :database-name (System/getenv "LEADS_DATABASE_NAME")})


(defonce ds (delay (make-datasource ds-options)))


(defn test-ds []
  (let [rows (jdbc/execute! @ds ["select 1 as test"])]
    (println rows)))


(defn create-dummy-lead []
  (sql/insert! @ds :leads {:id (UUID/randomUUID)
                           :status 623}))


(defn all-leads []
  (sql/query @ds ["select * from leads"]))


(defn find-lead [id]
  (sql/get-by-id @ds :leads (UUID/fromString id)))


(defn purge-lead-table []
  (jdbc/execute! @ds ["delete from leads"]))


(comment
  (test-ds)
  (create-dummy-lead)
  (create-dummy-lead)
  (let [id (-> (all-leads)
               first
               :leads/id
               .toString)]
    (find-lead id))
  )



















