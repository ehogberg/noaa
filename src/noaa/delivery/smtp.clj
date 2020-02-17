(ns noaa.delivery.smtp
  (:require [environ.core :refer [env]]
            [postal.core :as postal]))


(defn -service-details
  "Returns the target mailserver, user ID credentialed against
   the mail server and who the message is being sent as."
  []
  (format "SMTPDelivery: server %s, user: %s, sender: %s"
          (env :smtp-delivery-host)
          (env :smtp-delivery-user)
          (env :smtp-delivery-sender)))


(defn -deliver-noaa
  "Transmits a NOAA via email to a sandbox user
   (the user specified in the NOAA is ignored.)
   Useful for testing that mail does what you expect
   without accidentally spamming people."
  [{:noaas/keys [noaa_text] :as noaa}]
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
           :subject "Notice of adverse action on a recent application."
           :body noaa_text})))
