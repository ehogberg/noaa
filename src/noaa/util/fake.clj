(ns noaa.util.fake
  (:require [cheshire.core :refer [generate-string]]
            [nearinfinity.clj-faker.name :as name]
            [nearinfinity.clj-faker.address :as address]
            [nearinfinity.clj-faker.internet :as inet]))


;; Thank you, (https://clojuredocs.org/clojure.core/merge)...
(defn deep-merge [a & maps]
  (if (map? a)
    (apply merge-with deep-merge a maps)
    (apply merge-with deep-merge maps)))


(defn fake-lead-api-request
  ([] (fake-lead-api-request {}))
  ([customized-data]
   (deep-merge
    {:email (inet/safe-email)
     :socialSecurityNumber nil
     :personalInfo {:firstName (name/first-name)
                    :lastName (name/last-name)
                    :address {:streetAddress (address/street-address)
                              :city (address/city)
                              :zip (address/post-code)
                              :countryCode "US"}}
     :stateCode (address/state)}
    customized-data)))


(defn fake-lead []
  (let [fake-ssn "123456789"]
    {:status 200
     :version "5"
     :request (-> {:socialSecurityNumber fake-ssn}
                  fake-lead-api-request
                  generate-string) 
     :response_code 200
     :ssn fake-ssn}))


(comment
  (fake-lead)
  (take 5 (repeatedly fake-lead))
  )

















