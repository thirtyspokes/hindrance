# hindrance

A convenience library for working with a particularly inconvenient authentication mechanism (OAuth 2.0 JWT Bearer Credentials Flow).

This library assumes that the following details are present as environment variables (or any other place where [environ](https://github.com/weavejester/environ) can access them):

- `HINDRANCE_OAUTH_CLIENT_ID`: Your client_id, as assigned by your OAuth provider.
- `HINDRANCE_OAUTH_SHARED_SECRET`: The shared secret key assigned by your OAuth provider.
- `HINDRANCE_OAUTH_TOKEN_URL`: The URL to which you post your JWTs to receive an access token.

## Usage

This library provides a wrapper function for [clj-http](https://github.com/dakrone/clj-http) client functions, so that you can simply make requests as you normally would and the bearer access token will be transparently added to the request headers.

```clojure
(ns your-project.core
  (:require [hindrance.core :refer [with-oauth-token]]
            [clj-http.client :as client]))

;; Assuming you received the access token "super-good-token", the with-oauth-token wrapper
;; will adjust your request map to include Authorization: Bearer super-good-token in the headers.
(with-oauth-token client/get "https://www.some-authenticated-service.com")
```

If you just want the token for some other purpose and don't care about the convenience function:

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
