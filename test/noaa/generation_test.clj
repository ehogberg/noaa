(ns noaa.generation-test
  (:require [noaa.generation :as gen]
            [noaa.test-data :as data]
            [clojure.test :refer [deftest testing is]]))



(deftest template-type-test
  (testing "Uses Clarity report not present template when
            Clarity report is missing"
    (is (= "lead_clarity_rejection_no_clarity_report.txt"
           (gen/template-to-use data/lead-noaa))))
  (testing "Uses Clarity report present template when
            Clarity report is present"
    (is (= "lead_clarity_rejection_with_clarity_report.txt"
           (gen/template-to-use data/lead-noaa-with-clarity-report)))))


(deftest request-from-lead-test
  (testing "request-from-lead builds correct structure"
    (is (= data/lead-request
           (-> data/raw-noaa
               (gen/request-from-lead))))))


;; #PENDING
(deftest clarity-report-test)


(deftest generate-message-test
  (testing "Lead message w/ Clarity report"
    (is (->> data/lead-noaa-generating-clarity-report-message
             (gen/generate-noaa-text)
             :noaa-text
             (re-find #"We also obtained your credit score" )))))



(deftest ^:integration generate-integration-test
  (testing "Is an integration test"))
