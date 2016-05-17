(ns hindrance.core-test
  (:require [clojure.test :refer :all]
            [cheshire.core :refer :all]
            [clj-time.core :refer [now minus plus minutes]]
            [clj-http.client :as client]
            [hindrance.core :refer :all]))

(defn creds-fixture
  [f]
  (defcreds "testing-client-id" "testing-shared-secret" "http://your-oauth-provider.com")
  (f))

(use-fixtures :once creds-fixture)

(def example-token
  {:token "example" :expires (plus (now) (minutes 1))})

(def expired-token
  {:token "expired" :expires (minus (now) (minutes 5))})

(def future-token
  {:token "not-expired" :expires (plus (now) (minutes 5))})

(deftest get-access-token-test
  (testing "It fetches a token if there isn't one already."
    (with-redefs [request-access-token (fn [] example-token)]
      (is (= (get-access-token) "example"))))
  (testing "It will re-use a token, if the one it has hasn't expired."
    (with-redefs [hindrance.core/current-token (atom future-token)
                  request-access-token (fn [] example-token)]
      (is (= (get-access-token) "not-expired"))))
  (testing "It will get a new token, if the current one has expired."
    (with-redefs [hindrance.core/current-token (atom expired-token)
                  request-access-token (fn [] example-token)]
      (is (= (get-access-token) "example")))))

(deftest with-oauth-token-test
  (testing "It adds the token to the request option map."
    (with-redefs [client/post (fn [url option-map] option-map)
                  get-access-token (fn [] (:token example-token))]
      (is (= (with-oauth-token client/post "http://www.test.com" {:body "Testing"})
             {:body "Testing" :headers {:authorization "Bearer example"}}))))
  (testing "It initializes the option map if none is provided."
    (with-redefs [client/get (fn [url option-map] option-map)
                  get-access-token (fn [] (:token example-token))]
      (is (= (with-oauth-token client/get "http://test.com")
             {:headers {:authorization "Bearer example"}})))))

(def claim #'hindrance.core/claim)

(deftest environmental-config-test
  (testing "It reads environmental configuration."
    (is (= (:iss (claim)) "testing-client-id"))
    (is (= (:sub (claim)) "testing-client-id"))
    (is (= (:aud (claim)) "http://your-oauth-provider.com"))))
