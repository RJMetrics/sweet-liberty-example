(defproject com.rjmetrics/dogs-service "0.1.0-SNAPSHOT"
  :description "A RESTful service for dogs"
  :url "https://github.com/RJMetrics/dogs-service"
  :resource-paths ["resources/base"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [org.clojure/data.json "0.2.4"]
                 [com.rjmetrics.developers/sweet-liberty "0.1.0-SNAPSHOT"]
                 [lib-noir "0.8.0" :exclusions [hiccup]]
                 [compojure "1.1.6"]
                 [ring/ring-core "1.2.1"]
                 [ring-refresh "0.1.1"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [ring-middleware-format "0.3.2"]
                 [org.hsqldb/hsqldb "2.2.4"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.syslog4j/syslog4j "0.9.30"]
                 [org.slf4j/slf4j-log4j12 "1.6.1"]
                 [log4j/log4j "1.2.15" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]]
  :ring {:handler com.rjmetrics.dogs-service.core/handler
         :auto-reload? true
         :auto-refresh? true}
  :profiles {:uberjar {:uberjar-name "dogs-service.jar"
                       :aot [com.rjmetrics.dogs-service.core
                             com.rjmetrics.dogs-service.migrate]
                       :main com.rjmetrics.dogs-service.core}
             :dev {:dependencies [[midje "1.6.3"]
                                  [org.clojars.runa/conjure "2.2.0"]
                                  [clj-http-fake "0.7.8"]
                                  [ring-mock "0.1.5"]]
                   :plugins [[lein-ring "0.8.7"]
                             [lein-pprint "1.1.1"]
                             [lein-midje "3.1.3"]]}})
