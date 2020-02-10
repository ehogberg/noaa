(defproject noaa "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[camel-snake-kebab "0.4.1"]
                 [cheshire "5.10.0"]
                 [clojure.java-time "0.3.2"]
                 [com.draines/postal "2.0.3"]
                 [com.fzakaria/slf4j-timbre "0.3.19"]
                 [com.taoensso/timbre "4.10.0"]
                 [environ "1.1.0"]
                 [hikari-cp "2.10.0"]
                 [migratus "1.2.7"]
                 [nearinfinity/clj-faker "0.1.1"]
                 [org.clojure/clojure "1.10.0"]
                 [org.postgresql/postgresql "42.2.10"]
                 [seancorfield/next.jdbc "1.0.13"]
                 [selmer "1.12.18"]]
  :plugins [[lein-environ "1.1.0"]
            [migratus-lein "0.7.3"]]
  :main ^:skip-aot noaa.core
  :aliases {"generate-noaas" ["run" "-m" "noaa.core" "generate-noaas"]
            "identify-noaas" ["run" "-m" "noaa.core" "identify-noaas"]
            "send-noaas" ["run" "-m" "noaa.core" "send-noaas"]}
  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "com.postgresql.jdbc.Driver"
                  :subprotocol "postgresql"
                  :subname "//localhost/lead_zeppelin_dev"}}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})










