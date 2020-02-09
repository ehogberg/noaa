(defproject noaa "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[clojure.java-time "0.3.2"]
                 [com.fzakaria/slf4j-timbre "0.3.19"]
                 [com.taoensso/timbre "4.10.0"]
                 [environ "1.1.0"]
                 [hikari-cp "2.10.0"]
                 [migratus "1.2.7"]
                 [org.clojure/clojure "1.10.0"]
                 [org.postgresql/postgresql "42.2.10"]
                 [seancorfield/next.jdbc "1.0.13"]]
  :plugins [[lein-environ "1.1.0"]
            [migratus-lein "0.7.3"]]
  :main ^:skip-aot noaa.core
  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "com.postgresql.jdbc.Driver"
                  :subprotocol "postgresql"
                  :subname "//localhost/lead_zeppelin_dev"}}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})










