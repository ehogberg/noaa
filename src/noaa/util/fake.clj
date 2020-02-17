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


(defn fake-ssn
  "Generates a sequence of digits that superficially resembles
   a social security number.

   Note that the real thing's digits are positionally meaningful,
   which this implementation makes zero attempt to support."
  []
  (format "%s%s%s"
          (+ (rand-int 899) 100)
          (+ (rand-int 89) 10)
          (+ (rand-int 8999) 1000)))


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
  (let [ssn (fake-ssn)]
    {:status 200
     :version "5"
     :request (-> {:socialSecurityNumber ssn}
                  fake-lead-api-request
                  generate-string)
     :response_code 200
     :ssn ssn}))


(comment
  (fake-lead)
  (repeatedly 5 fake-lead)
  )
