# hindrance

A convenience library for working with a particularly inconvenient authentication mechanism (OAuth 2.0 JWT Bearer Credentials Flow).

Provided are two convenience functions for making authenticated reuqests using [clj-http](https://github.com/dakrone/clj-http):

```clojure
(ns your-project.core
   (:require [hindrance.core :refer [with-credentials]
             [clj.http :as client])

;; Provide your oauth credentials in the form of a map with the
;; following keys:
(def my-creds
  {:client-id "my-client-id"
   :shared-secret "some-long-shared-secret"
   :token-url "https://your-token-provider.com"})

;; Having done so, you can now use with-credentials to transparently make OAuth requests:
(with-credentials my-creds client/get "https://some.authenticated-service.com")
```

The above will first make a POST request to the identity provider and store the received token locally, then add it to the Authorization headers in your request.

If you prefer to load your configuration from the environment, there is an equivalent function `with-oauth-token` that assumes that the following details are present as environment variables (or any other place where [environ](https://github.com/weavejester/environ) can access them):

- `HINDRANCE_OAUTH_CLIENT_ID`: Your client_id, as assigned by your OAuth provider.
- `HINDRANCE_OAUTH_SHARED_SECRET`: The shared secret key assigned by your OAuth provider.
- `HINDRANCE_OAUTH_TOKEN_URL`: The URL to which you post your JWTs to receive an access token.

```clojure
(ns your-project.core
  (:require [hindrance.core :refer [with-oauth-token]]
            [clj-http.client :as client]))

;; This is equivalent to the example above, except the values for the identity provider request
;; will be read from environmental variables.
(with-oauth-token client/get "https://www.some-authenticated-service.com")
```

## Usage

In your project.clj: 

```
[thirtyspokes/hindrance "1.0.0"]
```

Or if your desires are *unconventional*:

```
<dependency>
  <groupId>thirtyspokes</groupId>
  <artifactId>hindrance</artifactId>
  <version>1.0.0</version>
</dependency>
```

Also, if you just want the token for some other purpose and don't care about the convenience function:

```clojure
(ns your-project.core
  (:require [hindrance.core :refer [get-access-token]]))

(get-access-token)
;; Will return your access token, as a string.
```

Hindrance will save the token you receive as an atom, and whenever `get-access-token` is called, it will re-use the token if it has not yet expired (based on the expiry time defined by your OAuth token provider), or request a brand-new one if it has.

## License

Copyright © 2016 Ray Ashman Jr.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
