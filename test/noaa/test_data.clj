(ns noaa.test-data)


;; #FIXME There are a lot of very specific types of noaa data structures
;; that look sorta alike.  Could probably narrow this catalog down a bit


(def raw-noaa
  {:raw-noaa
   {:leads/version nil,
    :noaas/noaa_destination_email nil,
    :leads/prepop_data nil,
    :leads/created_date #inst "2020-02-09T16:34:04.817329000-00:00",
    :leads/updated_date #inst "2020-02-09T16:34:04.817329000-00:00",
    :noaas/lead_id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
    :leads/ssn nil,
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
    "{\"email\":\"Aurelia86@example.com\",\"socialSecurityNumber\":\"123456789\",\"personalInfo\":{\"firstName\":\"Lisa\",\"lastName\":\"Marvin\",\"address\":{\"streetAddress\":\"54872 Collier Summit\",\"city\":\"Kertzmannmouth\",\"zip\":\"81861\",\"countryCode\":\"US\"}},\"stateCode\":\"Arizona\"}",
    :leads/status 623,
    :leads/id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
    :noaas/noaa_text nil}})


(def meta 
  {:current-date #inst "2020-02-09",
   :noaa-id #uuid "f8b4f241-6fac-4e86-8066-2889dd4c3999",
   :lead-id #uuid "f35eb07e-e95a-4973-977d-495b05d27fa9",
   :formatted-date "Feb 9, 2020"})


(def lead-meta (assoc meta :application-type :lead))


(def clarity-report
  {:ccr-reason-code-description
  "(10) You have a delinquency reported on an account|(20) Length of time since online payday loan opened|(15) Lack of sufficient relevant revolving or bankcard information|(36) Lack of sufficient relevant account information",
  :ccr-code 532,
  :clarity-generation-date "2019-07-12T19:49:50Z",
  :fraud-score 532,
  :credit-model-version "4.0",
  :cbb-score "835",
  :fraud-reason-code-description
  "(10) You have a delinquency reported on an account|(20) Length of time since online payday loan opened|(15) Lack of sufficient relevant revolving or bankcard information|(36) Lack of sufficient relevant account information",
  :denial-reason
  "(BB114(#)) Lack of or negative retail check writing history|(BB108(*)) Number of bank account closures in last 5 years|(BB110(*)) Number of bank account opening attempts  in last 3 years|(BB113) Length of time between bank account(s) being submitted",
  :company "FWBOpp27"})


(def lead-request
  {:email "Aurelia86@example.com",
   :ssn "123456789",
   :first-name "Lisa",
   :last-name "Marvin",
   :street-address "54872 Collier Summit",
   :city "Kertzmannmouth",
   :state "Arizona",
   :zip-code "81861"})


(def lead-noaa {:meta (assoc meta :application-type :lead)})


(def lead-noaa-with-clarity-report (assoc lead-noaa
                                          :clarity-report clarity-report))


(def lead-noaa-ready-for-message-generation
  {:meta lead-meta
   :clarity-report clarity-report
   :request lead-request})


(def lead-noaa-generating-clarity-report-message
  (assoc-in lead-noaa-ready-for-message-generation
            [:meta :template-type]
            "lead_clarity_rejection_with_clarity_report.txt"))










