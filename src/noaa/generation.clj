(ns noaa.generation
  (:require [camel-snake-kebab.core :as csk]
            [cheshire.core :refer [parse-string]]
            [java-time :refer [local-date] :as time]
            [noaa.visine :refer [get-cached-clarity-report]]
            [selmer.parser :as selmer]))

(selmer/set-resource-path! (clojure.java.io/resource "templates"))


;; Useful for starting a generation in the repl.
(def std-noaa-db-data
  {:raw-noaa
   {:leads/version nil,
    :leads_noaas/noaa_destination_email nil,
    :leads/prepop_data nil,
    :leads/created_date #inst "2020-02-09T16:34:04.817329000-00:00",
    :leads/updated_date #inst "2020-02-09T16:34:04.817329000-00:00",
    :leads_noaas/lead_id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
    :leads/ssn nil,
    :leads_noaas/created_at
    #inst "2020-02-09T16:34:20.539184000-00:00",
    :leads/response nil,
    :leads_noaas/id #uuid "f8b4f241-6fac-4e86-8066-2889dd4c3999",
    :leads/all_ctx nil,
    :leads_noaas/noaa_identified_at
    #inst "2020-02-09T16:34:20.538000000-00:00",
    :leads_noaas/noaa_generated_at nil,
    :leads_noaas/updated_at
    #inst "2020-02-09T16:34:20.539184000-00:00",
    :leads_noaas/noaa_transmitted_at nil,
    :leads/response_code nil,
    :leads/request
    "{\"email\":\"Aurelia86@example.com\",\"socialSecurityNumber\":\"123456789\",\"personalInfo\":{\"firstName\":\"Lisa\",\"lastName\":\"Marvin\",\"address\":{\"streetAddress\":\"54872 Collier Summit\",\"city\":\"Kertzmannmouth\",\"zip\":\"81861\",\"countryCode\":\"US\"}},\"stateCode\":\"Arizona\"}",
    :leads/status 623,
    :leads/id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
    :leads_noaas/noaa_text nil}})


;; A completely-filled-in generated NOAA structure, based on the above.
(def std-generated-noaa
  {:meta
   {:current-date #inst "2020-02-09",
    :application-type :lead,
    :noaa-id #uuid "f8b4f241-6fac-4e86-8066-2889dd4c3999",
    :lead-id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
    :formatted-date "Feb 9, 2020"},
   :request
   {:email "Aurelia86@example.com",
    :ssn "123456789",
    :first-name "Lisa",
    :last-name "Marvin",
    :street-address "54872 Collier Summit",
    :city "Kertzmannmouth",
    :state "Arizona",
    :zip-code "81861"},
   :clarity-report {:clear_fraud_score 801},
   :noaa-text
   "(Message text truncated for code readability..."})


(defn template-to-use [noaa]
  (case (get-in noaa [:meta :application-type])
    :lead (if (empty? (:clarity-report noaa))
            "lead_clarity_rejection_no_clarity_report.txt"
            "lead_clarity_rejection_with_clarity_report.txt")
    :else nil))


(defn attach-meta [{:keys [raw-noaa] :as noaa}]
  (assoc noaa :meta {:current-date (local-date)
                     :application-type :lead
                     :noaa-id (:leads_noaas/id raw-noaa)
                     :lead-id (:leads/id raw-noaa)
                     :formatted-date (time/format "MMM d, YYYY",
                                                  (local-date))}))

(defn set-template-type [noaa]
  (assoc-in noaa [:meta :template-type]
            (template-to-use noaa)))


(defn attach-clarity-report [{:keys [ssn] :as noaa}]
  (let [report (get-cached-clarity-report ssn)
        visine-body (if-not (empty? report)
                      {:clear_fraud_score
                       (get-in report
                               [:xml_response :clear_fraud :clear_fraud_score])}
                      {})]
    (assoc noaa :clarity-report visine-body)))


(defn attach-noaa-text [{:keys [template-type] :as noaa}]
  (assoc noaa :noaa-text
         (selmer/render-file template-type noaa)))


(defn extract-json-text
  "The things we do to navigate JSON...
   Simple converter facade used to first attempt
   to extract a value from a suspected JSON field.
   If .getValue fails, it's likely some variety of
   text, try that instead."
  [v]
  (try
    (.getValue v)
    (catch Exception e
      (.toString v))))


(defn request-from-lead [lead]
  (let [{:keys [email social-security-number state-code]
         {:keys [first-name last-name]
          {:keys [street-address city zip]} :address} :personal-info
         :as lead_request}
        (parse-string
         (extract-json-text (get-in lead [:raw-noaa :leads/request])) 
         (fn [k] (csk/->kebab-case-keyword k)))]
     {:email email
      :ssn   social-security-number
      :first-name first-name
      :last-name  last-name
      :street-address street-address
      :city city
      :state state-code
      :zip-code zip}))


(defn assemble-request [{{:keys [application-type]} :meta :as noaa}]
  (case application-type
    :lead (request-from-lead noaa )))


(defn normalize-noaa-data [noaa]
  (-> noaa
      (assoc :request (assemble-request noaa))
      (dissoc :raw-noaa)))


(defn generate-noaa-text [{{:keys [template-type]} :meta 
                           :as noaa}]
  (assoc noaa :noaa-text
         (selmer/render-file template-type noaa)))



(defn process-noaa-generation [noaa]
  (-> noaa
      attach-meta
      normalize-noaa-data
      attach-clarity-report
      set-template-type
      generate-noaa-text))


(comment
  (-> std-noaa-db-data
      attach-meta
      normalize-noaa-data
      attach-clarity-report
      set-template-type
      )
  (process-noaa-generation std-noaa-db-data)
  )









