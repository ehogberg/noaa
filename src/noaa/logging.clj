(ns noaa.logging
  (:require [clojure.string :refer [upper-case]]
   [taoensso.timbre :refer [with-context stacktrace]]))


(defn timbre-output-with-job-id
  "Somewhat odorous attempt to attach a constant transaction id
   across Timbre logging output.  FIXME please; there's got to
   be a better way of doing this...  Boy, it would be nice if
   this sorta-fabulous Timbre library were documented a wee bit
   more practically."
  ([event] (timbre-output-with-job-id nil event) )
  ([{:keys [no-stacktrace?] :as opts}
    {:keys [level err? msg_ timestamp_ ?line]
     {:keys [job-id]} :context
     :as event}]
   (str
    (force timestamp_) " "
    (upper-case (name level)) " "
    "[" job-id "] - "
    (force msg_)
    (when-not no-stacktrace?
      (when-let [err err?]
        (str "\n" (stacktrace err opts)))))))
