(ns hindrance.core-test
  (:require [clojure.test :refer :all]
            [cheshire.core :refer :all]
            [clj-time.core :refer [now minus plus minutes]]
            [clj-http.client :as client]
            [hindrance.core :refer :all]))

(def example-token
  {:token "example" :expires (plus (now) (minutes 1))})

(def expired-token
  {:token "expired" :expires (minus (now) (minutes 5))})

(def future-token
  {:token "not-expired" :expires (plus (now) (minutes 5))})

(deftest get-access-token-test
  (testing "It fetches a token if there isn't one already."
    (with-redefs [request-access-token (fn [creds] example-token)]
      (is (= (get-access-token (credentials-from-env)) "example"))))
  (testing "It will re-use a token, if the one it has hasn't expired."
    (with-redefs [hindrance.core/tokens (atom {"testing-client-id" future-token})
                  request-access-token (fn [creds] example-token)]
      (is (= (get-access-token (credentials-from-env)) "not-expired"))))
  (testing "It will get a new token, if the current one has expired."
    (with-redefs [hindrance.core/tokens (atom {"testing-client-id" expired-token})
                  request-access-token (fn [creds] example-token)]
      (is (= (get-access-token (credentials-from-env)) "example")))))

(deftest with-oauth-token-test
  (testing "It adds the token to the request option map using the environment vars."
    (with-redefs [client/post (fn [url option-map] option-map)
                  get-access-token (fn [creds] (:token example-token))]
      (is (= (with-oauth-token client/post "http://www.test.com" {:body "Testing"})
             {:body "Testing" :headers {:authorization "Bearer example"}}))))
  (testing "It initializes the option map if none is provided."
    (with-redefs [client/get (fn [url option-map] option-map)
                  get-access-token (fn [creds] (:token example-token))]
      (is (= (with-oauth-token client/get "http://test.com")
             {:headers {:authorization "Bearer example"}})))))

(def test-creds
  {:client-id "testing-client-id"
   :shared-secret "testing-shared-secret"
   :token-url "http://your-oauth-provider.com"})

(def other-test-creds
  {:client-id "testing-other-client-id"
   :shared-secret "testing-other-shared-secret"
   :token-url "http://your-oauth-provider.com"})

(def test-storage (atom {}))

(deftest with-creds-test
  (testing "It adds the token to the request option map using the environment vars."
    (with-redefs [client/post (fn [url option-map] option-map)
                  get-access-token (fn [creds] (:token example-token))]
      (is (= (with-credentials test-creds client/post "http://www.test.com" {:body "Testing"})
             {:body "Testing" :headers {:authorization "Bearer example"}}))))
  (testing "It can store multiple different client IDs"
    (with-redefs [client/post (fn [url option-map] option-map)
                  request-access-token (fn [creds] "hello")
                  hindrance.core/tokens test-storage]
      (do
        (with-credentials test-creds client/post "http://www.test.com" {:body "Testing"})
        (with-credentials other-test-creds client/post "http://www.test.com" {:body "Testing"})
        (is (= (count @test-storage) 2)))))
  (testing "It initializes the option map if none is provided."
    (with-redefs [client/get (fn [url option-map] option-map)
                  get-access-token (fn [creds] (:token example-token))]
      (is (= (with-credentials test-creds client/get "http://test.com")
             {:headers {:authorization "Bearer example"}})))))

(deftest response-structure-test
  (testing "Handles the 'old' token structure and the 'new'."
    (with-redefs [hindrance.core/make-token-request (fn [x] {:body (generate-string {:access_token "my-token" :expires_in "1"})})]
      (is (= (:token (request-access-token {})) "my-token")))
    (with-redefs [hindrance.core/make-token-request (fn [x] {:body (generate-string {:data {:access_token "my-token" :expires_in "1"}})})]
      (is (= (:token (request-access-token {})) "my-token")))))

(deftest environmental-config-test
  (testing "It reads environmental configuration."
    (is (= (:iss (claim (credentials-from-env))) "testing-client-id"))
    (is (= (:sub (claim (credentials-from-env))) "testing-client-id"))
    (is (= (:aud (claim (credentials-from-env))) "http://your-oauth-provider.com"))))
