(defproject thirtyspokes/hindrance "1.1.0"
  :description "A convenience wrapper for using OAuth JWT credentials flow with clj-http."
  :url "https://github.com/thirtyspokes/hindrance"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.0.1"]
                 [environ "1.0.2"]
                 [clj-jwt "0.1.1"]
                 [clj-time "0.11.0"]
                 [cheshire "5.5.0"]]
  :plugins [[lein-environ "1.0.2"]]
  :profiles {:test {:env {:hindrance-oauth-client-id "testing-client-id"
                          :hindrance-oauth-shared-secret "testing-shared-secret"
                          :hindrance-oauth-token-url "http://your-oauth-provider.com"}}})
