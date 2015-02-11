(ns com.rjmetrics.dogs-service.db
  (:require [clojure.java.jdbc :as j]))

;; This namespace contains functions for building an in-memory db based
;; on a sweet-liberty configuration map. This is only for demonstration
;; purposes and not intended for any kind of real-world use.

(defn keyword-to-str
  [keyword]
  (subs (str keyword) 1))

(defn build-column-defs
  [attributes]
  (mapv #(if (= :id %)
          [% "INTEGER" "NOT NULL" "IDENTITY"]
          [% "VARCHAR(255)"])
        attributes))

(defn create-table
  [db-spec table-name attributes]
  (j/execute! db-spec [(apply j/create-table-ddl
                              table-name
                              (build-column-defs attributes))]))

(defn populate-table
  [db-spec table-name init-data]
  (if (> (count init-data) 1)
    (apply j/insert! db-spec table-name init-data )))

(defn drop-table
  [db-spec table-name]
  ;; I specifically opted not to use the ddl lib here since it doesn't support IF EXISTS
  (j/execute! db-spec [(str "DROP TABLE IF EXISTS " (keyword-to-str table-name))]))

(defn initialize-table
  [db-spec {:keys [table-name attributes]} init-data]
  (drop-table db-spec table-name)
  (create-table db-spec table-name attributes)
  (populate-table db-spec table-name init-data))

(defn extract-table-defs
  [config]
  (map #(-> %
           second
           :table)
       config))

(defn initialize-db
  [db-spec config init-data]
  (dorun (for [table-def (extract-table-defs config)]
           (initialize-table db-spec table-def ((:table-name table-def) init-data)))))
