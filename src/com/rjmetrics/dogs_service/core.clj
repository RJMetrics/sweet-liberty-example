(ns com.rjmetrics.dogs-service.core
  (:require [com.rjmetrics.sweet-liberty.core :as sl]
            [compojure.core :refer [defroutes GET POST PUT ANY]]
            [compojure.route :as route]
            [noir.util.middleware :as middleware]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [com.rjmetrics.dogs-service.db :as db]
            [liberator.dev :refer [wrap-trace]])
  (:gen-class))

(def config
  "Sweet-Liberty config values"
  {:dogs {:table {:table-name :dogs
                  :attributes [:id
                               :name
                               :breed
                               ;; additional db columns go here
                               ]
                  :primary-key :id}}})

(def init-data
  "This is sample data to populate db with. It is a map where the key is the
  name of the table and the value is a vector of vectors. Each inner vector
  contains the data for a row."
  {:dogs [["id" "name" "breed"]
          [1 "Fido" "poodle"]
          [2 "Lacy" "corgi"]
          [3 "Rex" "chihuahua"]
          [4 "Spot" "dalmation"]
          [5 "Taco" "chihuahua"]
          [6 "Brody" "corgi"]]})

;; The specification for connecting to our database.
;; For this demo, we're using an in-memory db.
;;; Typically, this would point to an RDBMS (like MySQL or Postgres)
(def db-spec
  {:subprotocol "hsqldb"
   :subname "mem:sweet-lib-example"})

;; Build and populate in-memory database
(db/initialize-db db-spec config init-data)

;; You can provide liberator configuration to sweet-liberty.
;; In this case, we are only setting the available media types.
;; see http://liberator... for details
(def liberator-config {:available-media-types ["application/json" "text/html"]})

(defroutes app
  (GET "/dogs" []
       (-> {:options (assoc (:dogs config) :db-spec db-spec :return-exceptions? true)
            :liberator-config liberator-config}
           sl/add-exists
           sl/add-get
           (sl/add-ok-handler :collection? true)

           ;; Sweet-Liberty requires that you explicitly set the authorization
           ;; handler. It's for your own good. The following line sets the
           ;; route to allow all requests -- ie the route is open to the public.
           (sl/add-authorization (constantly true))
           sl/make-resource))
  (POST "/dogs" []
        (-> {:options (assoc (:dogs config) :db-spec db-spec)
             :liberator-config liberator-config}
            (sl/add-exists :id)
            (sl/add-ok-handler :collection? true)
            sl/add-post
            sl/add-created-handler
            (sl/add-authorization (constantly true))
            sl/make-resource))
  (GET "/dogs/:id" [id]
       (-> {:options (assoc (:dogs config) :db-spec db-spec :url-params {:id id})
            :liberator-config liberator-config}
           (sl/add-exists :id)
           sl/add-get
           (sl/add-ok-handler :collection? false)
           (sl/add-authorization (constantly true))
           sl/make-resource))
  (PUT "/dogs/:id" [id]
       (-> {:options (assoc (:dogs config) :db-spec db-spec :url-params {:id id})
            :liberator-config liberator-config}
           (sl/add-exists :id)
           (sl/add-ok-handler :collection? false)
           (sl/add-put :id)
           (sl/add-authorization (constantly true))
           sl/make-resource))
  (route/not-found "Can't find route"))

(def handler (middleware/app-handler
              ;; add your application routes here
              [#'app]
              ;; add custom middleware here
              :middleware [wrap-params
                           ;; (fn [handler & [_]] (wrap-trace handler :header :ui)) ;; uncomment to add liberator debug headers to response
                           ]
              ;; add access rules here
              :access-rules []
              ;; serialize/deserialize the following data formats
              ;; available formats:
              ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
              :formats [:json :json-kw]))
