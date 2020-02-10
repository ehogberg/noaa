(ns noaa.delivery.smtp
  (:require [environ.core :refer [env]]
            [postal.core :as postal]))


(defn _deliver-noaa [{:leads_noaas/keys [noaa_text] :as noaa}]
  (assoc noaa :delivery-status
         (postal/send-message
          {:host (env :smtp-delivery-host)
           :user (env :smtp-delivery-user)
           :pass (env :smtp-delivery-password)
           :port (Integer/parseInt (or (env :smtp-delivery-port)
                                       587))
           :tls (env :smtp-delivery-tls)}
          {:from (env :smtp-delivery-sender)
           :to (env :smtp-delivery-sandbox-recipient)
           :subject "Notice of adverse action on a recent OppLoans application."
           :body noaa_text})))










