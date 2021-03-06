(ns noaa.generation
  (:require [camel-snake-kebab.core :as csk]
            [cheshire.core :refer [parse-string]]
            [clojure.string :as string]
            [java-time :refer [local-date
                               offset-date-time] :as time]
            [noaa.visine :refer [get-cached-clarity-report]]
            [selmer.filters :refer [add-filter!]]
            [selmer.parser :as selmer]))



;; Important: don't monkey with this unless you grok Selmer and
;; are needing to retrieve noaa message templates from a location
;; other than the default ($PROJECT_ROOT/resources/templates)
(selmer/set-resource-path! (clojure.java.io/resource "templates"))


(defn format-clarity-reasons
  "A very simple Selmer filter function.  Takes a string
   representing a list of Clarity denial reason codes, each reason
   deliminated by a | char, split along the | and add a newline
   between each item."
  [s]
  (string/replace s "|" "\n"))

(add-filter! :format-clarity-reasons format-clarity-reasons)



;; Useful for starting a generation in the repl.
(def std-noaa-db-data
  {:raw-noaa
   {:leads/version nil,
    :noaas/noaa_destination_email nil,
    :leads/prepop_data nil,
    :leads/created_date #inst "2020-02-09T16:34:04.817329000-00:00",
    :leads/updated_date #inst "2020-02-09T16:34:04.817329000-00:00",
    :noaas/lead_id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
    :leads/ssn "123456780",
    :noaas/created_at
    #inst "2020-02-09T16:34:20.539184000-00:00",
    :leads/response nil,
    :noaas/id #uuid "f8b4f241-6fac-4e86-8066-2889dd4c3999",
    :leads/all_ctx nil,
    :noaas/noaa_identified_at
    #inst "2020-02-09T16:34:20.538000000-00:00",
    :noaas/noaa_generated_at nil,
    :noaas/updated_at
    #inst "2020-02-09T16:34:20.539184000-00:00",
    :noaas/noaa_transmitted_at nil,
    :leads/response_code nil,
    :leads/request
    "{\"email\":\"Aurelia86@example.com\",
\"socialSecurityNumber\":\"123456780\",\"personalInfo\":
{\"firstName\":\"Lisa\",\"lastName\":\"Marvin\",\"address\":
{\"streetAddress\":\"54872 Collier Summit\",\"city\":\"Kertzmannmouth\",
\"zip\":\"81861\",\"countryCode\":\"US\"}},\"stateCode\":\"Arizona\"}"
    :leads/status 623,
    :leads/id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
    :noaas/noaa_text nil}})


;; A completely-filled-in generated NOAA structure, based on the above.
(def std-generated-noaa
  {:meta
   {:current-date #inst "2020-02-09",
    :application-type :lead,
    :noaa-id #uuid "f8b4f241-6fac-4e86-8066-2889dd4c3999",
    :lead-id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
    :template-type  "lead_clarity_rejection_with_clarity_report.txt"
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
   :clarity-report
   {:ccr-reason-code-description
    "(10) You have a delinquency reported on an account|
(20) Length of time since online payday loan opened|
(15) Lack of sufficient
relevant revolving or bankcard information|
(36) Lack of sufficient relevant
account information",
    :ccr-code 532,
    :clarity-generation-date ["2019-04-09T17:00:04.809Z"],
    :fraud-score 532,
    :credit-model-version "4.0",
    :cbb-score "835",
    :fraud-reason-code-description
    "(10) You have a delinquency reported on an account|
(20) Length of time since online payday loan opened|
(15) Lack of sufficient relevant revolving or bankcard information|
(36) Lack of sufficient relevant account information",
    :denial-reason
    "(BB114(#)) Lack of or negative retail check writing history|
(BB108(*)) Number of bank account closures in last 5 years|
(BB110(*)) Number of bank account opening attempts  in last 3 years|
(BB113) Length of time between bank account(s) being submitted",
    :company :finwise}
   :noaa-text
   "(Message text truncated for code readability..."})


(defn standard-date-string
  "Helper to standardize output display of dates."
  [d]
  (time/format "MMM d, YYYY" d))


(defn template-to-use
  "given a noaa, determines the correct noaa template
   to be used for message generation.  Currently supports
   only lead-based noaas with or without clarity"
  [noaa]
  (case (get-in noaa [:meta :application-type])
    :lead (if (empty? (:clarity-report noaa))
            "lead_clarity_rejection_no_clarity_report.txt"
            "lead_clarity_rejection_with_clarity_report.txt")
    nil))


(defn attach-meta
  "Fills in the meta section of the generating noaa payload."
  [{:keys [raw-noaa] :as noaa}]
  (assoc noaa :meta {:current-date (local-date)
                     :application-type :lead
                     :noaa-id (:leads_noaas/id raw-noaa)
                     :lead-id (:leads/id raw-noaa)
                     :formatted-date (standard-date-string (local-date))}))


(defn set-template-type
  "General purpose function which determines the
   proper message template to use for a noaa then
   makes sure the template name is attached to the
   generating noaa payload meta section"
  [noaa]
  (assoc-in noaa [:meta :template-type]
            (template-to-use noaa)))



(defn build-visine-data
  "Does the detailed extraction of relevant Clarity
   data from the (much larger) complete report."
  [{{{credit-model-version :pass_through_5
      control-file-name :control_file_name} :inquiry
     {fraud-score :score
      fraud-reason-code-description :reason_code_description} :clear_credit_risk
     {cbb-score :cbb_score
      denial-reason :cbb_reason_code_description} :clear_bank_behavior
     {report-generation-at :clarity_original_pull_time} :opploans}
    :xml_response}]
  {:clarity-generation-date (-> report-generation-at
                                offset-date-time
                                standard-date-string)
   :fraud-score fraud-score
   :fraud-reason-code-description fraud-reason-code-description
   :credit-model-version credit-model-version
   :cbb-score cbb-score
   :denial-reason denial-reason
   :company (if (string/starts-with? control-file-name "FWB")
              :finwise
              :core)})


(defn attach-clarity-report
  "Retrieves the Clarity report (if any) for the noaa
   and attaches it to the noaa's generation payload for
   potential use in message generation."
  [{{:keys [ssn]} :request :as noaa}]
  (let [report (get-cached-clarity-report ssn)
        visine-body
        (if-not (empty? report)
          (build-visine-data report)
          {})]
    (assoc noaa :clarity-report visine-body)))



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
      (str v))))


(defn request-from-lead
  "Takes raw lead data and normalizes it into a request info
   structure for potential subsequent use by the templating
   system.  The LDE4/LZ lead schema is deeply and somewhat oddly
   nested, hence the eyeball-straining destructuring below used
   to pluck the various fields."
  [lead]
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


(defn assemble-request
  "General function responsible for assembling a request information
   section and attaching it to the generating noaa payload advancing
   through the generation process.  The actual assembly of the section
   is dependent on the type of noaa being generated and is delegated
   to a type-appropriate assembly function."
  [{{:keys [application-type]} :meta :as noaa}]
  (case application-type
    :lead (request-from-lead noaa )))


(defn normalize-noaa-data
  "Begins the assembly of the generating noaa payload.  Creates
   and attaches the request section of the structure and detaches
   any raw noaa data present."
  [noaa]
  (-> noaa
      (assoc :request (assemble-request noaa))
      (dissoc :raw-noaa)))


(defn generate-noaa-text
  "Last step in noaa generation processing: generates the message
   text using the Selmer templating system and attaches the message
   to the generating noaa payload; downstream from here the message text
   and recipient will be saved to the noaa db for delivery during a
   subsequent send-noaas process.

   Discussion of how Selmer templating works is beyond the scope of this
   comment string; however anyone intending to support the noaa system is
   well advised to familiarize themselve with it.  A good starting resource
   is the project homepage (https://github.com/yogthos/Selmer)"
  [{{:keys [template-type]} :meta
                           :as noaa}]
  (assoc noaa :noaa-text
         (selmer/render-file template-type noaa)))



(defn process-noaa-generation
  "The driver process for noaa generation.  Given a raw noaa read from
   the db, a series of transformations are applied to it which normalize
   its content, add some derived meta data values, retrieves and attaches
   relevant data from external systems (e.g., Clarity/Visine) and finally
   passes itself to the templating system for use as merge inputs to the
   templated message generation.   A before and after example of the effects
   of this transformation can be seen in the standard-noaa-db-data and
   std-generated-noaa vars at the top of this namespace.

   NB: Be very very wary of changing the order of threaded processing for this
   structure; later steps in the process often depend on data associated with
   the traveling map at earlier steps and very weird errors can occur for
   seemingly no reason.  An excellent IMPROVEME would be appropriate exception
   generation by threading steps which don't find the state they are depending
   on.

   Also, if/when adding a new step in the threading, remember that your new
   step *must* return the map passed into it, with whatever adjustments you've
   made to it in the step."
  [noaa]
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
      generate-noaa-text
      )
  (process-noaa-generation std-noaa-db-data)
  )
