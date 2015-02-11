(ns com.rjmetrics.dogs-service.dogs-test
  (:require [midje.sweet :refer :all]
            [com.rjmetrics.dogs-service.core :as sc]
            [ring.mock.request :as mock-req]
            [clojure.data.json :as json]
            [com.rjmetrics.dogs-service.db :as db]))

(defn to-json-input-stream
  "Take a vector or map, write it using json/write-str, and put it into a
  ByteArrayInputStream. Necessary because a compojure handler expects the
  request body in this form."
  [items]
  (java.io.ByteArrayInputStream. (.getBytes (json/write-str items))))

(defn from-json-key
  "Converts a JSON key from a string to a keyword, replacing any underscores
  with a dash. Does not affect capitalization of the string.
  (from-json-key \"My_Key\")
  => :My-Key"
  [k] (-> k (clojure.string/replace \_ \-) keyword))

(defn from-json
  "Parses a JSON string while converting any keys using the from-json-key
  function."
  [s] (json/read-str s :key-fn from-json-key))


(defn mk-request
  [method url]
  (sc/handler (mock-req/request method url)))

(with-state-changes [(before :facts (db/initialize-db sc/db-spec
                                                      sc/config
                                                      sc/init-data))]
  (fact-group "about GET /dogs"
              (fact "collection route return status 200"
                    (mk-request :get "/dogs")
                    => (contains {:status 200}))
              (fact "collection route returns all values"
                    (-> (mk-request :get "/dogs") :body from-json)
                    => [{:breed "poodle", :id 1, :name "Fido"}
                        {:breed "corgi", :id 2, :name "Lacy"}
                        {:breed "chihuahua", :id 3, :name "Rex"}
                        {:breed "dalmation", :id 4, :name "Spot"}
                        {:breed "chihuahua", :id 5, :name "Taco"}
                        {:breed "corgi", :id 6, :name "Brody"}]

                    )
              (fact "collection route returns filtered results"
                    (-> (mk-request :get "/dogs?breed=chihuahua") :body from-json)
                    => [{:breed "chihuahua", :id 3, :name "Rex"}
                        {:breed "chihuahua", :id 5, :name "Taco"}]))

  (fact-group "about GET /dogs/:id"
              (fact "successful GET returns status 200"
                    (mk-request :get "/dogs/1")
                    => (contains {:status 200}))
              (fact "resource returned successfully"
                    (-> (mk-request :get "/dogs/1") :body from-json)
                    => {:breed "poodle", :id 1, :name "Fido"})
              (fact "requesting non-existant resource returns status 404"
                    (:status (mk-request :get "/dogs/100"))
                    => 404))

  (fact-group "about PUT /dogs/:id"
              (fact "resource returned successfully"
                    (let [result (-> (mock-req/request :put "/dogs/2")
                                     (assoc :body (to-json-input-stream
                                                   {:name "Rocco"}))
                                     (mock-req/content-type "application/json")
                                     sc/handler
                                     :body
                                     from-json)]
                      result
                      => {:id 2 :breed "corgi" :name "Rocco"}))

              (fact "a PUT request to a non-existant resource returns status 501 Not Implemented"
                    (let [result (-> (mock-req/request :put "/dogs/1000")
                                     (assoc :body (to-json-input-stream
                                                   {:name "Rocco"}))
                                     (mock-req/content-type "application/json")
                                     sc/handler
                                     :status)]
                      result
                      => 501)))

  (fact-group "about POST /dogs"
              (fact "create a new resource with POST"
                    (:status (mk-request :get "/dogs/7"))
                    => 404 ;; resource does not exist

                    (-> (mock-req/request :post "/dogs")
                        (assoc :body (to-json-input-stream
                                      {:name "Jojo" :breed "poodle"}))
                        (mock-req/content-type "application/json")
                        sc/handler
                        :body
                        from-json)
                    => {:id 7 :breed "poodle" :name "Jojo"} ;; correct response

                    (-> (mk-request :get "/dogs/7")
                        :body
                        from-json)
                    => {:breed "poodle", :id 7, :name "Jojo"})
              (fact "a POST request to an existing resource returns status 405 Method Not Allowed"
                    (-> (mock-req/request :post "/dogs/2")
                        (assoc :body (to-json-input-stream
                                      {:name "Rocco"}))
                        (mock-req/content-type "application/json")
                        sc/handler
                        :status)
                    => 404))) ;; returning status 405 "Method Not Allowed",
                              ;; instead of 404, would be more correct here, but I didn't define the route to
                              ;; handle that, for the sake of brevity.
