(ns hindrance.core
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [environ.core :refer [env]]
            [clj-jwt.core :refer :all]
            [clj-time.core :as t]))

(def ^:private tokens (atom {}))

(defn credentials-from-env
  "A map of credentials loaded from the execution environment, for those
   that prefer the 'easy' method."
  []
  {:client-id (env :hindrance-oauth-client-id)
   :shared-secret (env :hindrance-oauth-shared-secret)
   :token-url (env :hindrance-oauth-token-url)})

(defn claim
  [creds]
  {:iss (:client-id creds)
   :sub (:client-id creds)
   :aud (:token-url creds)
   :exp (t/plus (t/now) (t/minutes 30))
   :iat (t/now)})

(defn- build-jwt
  "Constructs the JWT from the configured parameters and signs it
  with HMAC-SHA256."
  [creds]
  (-> (claim creds)
      jwt
      (sign :HS256 (:shared-secret creds))
      to-str))

(defn- make-token-request
  "POSTs the JWT to the configured token URL and returns the
  response map unchanged."
  [creds]
  (client/post
   (:token-url creds)
      {:form-params 
       {:grant_type "client_credentials"
        :client_asertion_type "urn:params:oauth:client-assertion-type:jwt-bearer"
        :client_id (:client-id creds)
        :client_assertion (build-jwt creds)}}))

(defn request-access-token
  "Requests an access token from the provider and parses the response into
   a map containing the access token itself, and the expiry time of the token
   as defined by the provider.

  Assumes that your OAuth provider will return the expiry time as number of
  seconds from 'now'."
  [creds]
  (let [response (parse-string (:body (make-token-request creds)) true)]
    {:token (:access_token response)
     :expires (t/plus (t/now) (t/seconds (Integer/parseInt (:expires_in response))))}))

(defn get-access-token
  "Returns the 'current' access token, fetching a new token from 
   the provider if the current token has expired.

  This function can be used if you aren't using clj-http methods, or if you
  have need for an access token for something more complex than just
  including in the Authorization header."
  [creds]
  (let [id (:client-id creds)
        token (get @tokens id)]
    (if (or (nil? token)
            (t/after? (t/now) (:expires token))) 
      (let [new-token-map (request-access-token creds)
            new-token (:token new-token-map)
            expires (:expires new-token-map)]
        (swap! tokens assoc id {:token new-token :expires expires})
        new-token)
      (:token (get @tokens id)))))

(defn with-oauth-token
  "Wraps a clj-http request function, setting the Authorization header of the
   request to contain an OAuth JWT Bearer token.  The token will either be one
   that has been previously requested, or a brand-new one if one hasn't been
   requested yet or the current one has expired.

   The request to get the OAuth token will inherit the :throw-exceptions setting
   being used to make the main request."
  [func url & opt-map]
  (let [options (into {} (first opt-map))
        creds (credentials-from-env)]
    (func url (assoc-in options [:headers :authorization] (str "Bearer " (get-access-token creds))))))

(defn with-credentials
  "Does the same as with-oauth-token, but accepts the credentials to be used as 
   an explicit first parameter than reading from environmental configuration.

   The tokens are stored using the :client-id from the credentials map as the
   key."
  [creds func url & opt-map]
  (let [options (into {} (first opt-map))]
    (func url (assoc-in options [:headers :authorization] (str "Bearer " (get-access-token creds))))))
