(defproject hindrance "0.1.0-SNAPSHOT"
  :description "A convenience wrapper for using OAuth JWT credentials flow with clj-http."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-http "2.0.1"]
                 [environ "1.0.2"]
                 [clj-jwt "0.1.1"]
                 [clj-time "0.11.0"]
                 [cheshire "5.5.0"]]
  :plugins [[lein-environ "1.0.2"]]
  :profiles {:test {:env {:hindrance-oauth-client-id "testing-client-id"
                          :hindrance-oauth-shared-secret "testing-shared-secret"
                          :hindrance-oauth-token-url "http://your-oauth-provider.com"}}})
