(ns mpisanko.index
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [mpisanko.indexing.indexer :as indexer]
            [mpisanko.indexing.organisation]
            [mpisanko.indexing.user]
            [mpisanko.indexing.ticket])
  (:import (java.io IOException)))

(defn- read-decode [file]
  (try
    (-> file
        slurp
        (json/decode true))
    (catch IOException e
      (log/errorf e "Exception reading file %s" file)
      (throw (ex-info (str "Problem reading file " file) {:problem "read-file"
                                                          :target  file} e)))))

(defn- write-edn [file payload]
  (try
    (with-open [w (io/writer file)]
      (binding [*out* w]
        (pr payload)))
    (catch IOException e
      (log/errorf e "Exception writing index file %s. Please make sure that directory where you run application is writable and there is sufficient disk space." file)
      (throw (ex-info (str "Problem writing file " file) {:problem "write-file"
                                                          :target  file} e)))))

(defn create
  "Create inverted indices for each of the entities (organisations, users, tickets)
   Indices also contain associated entities"
  []
  (let [organisations (read-decode "organizations.json")
        users (read-decode "users.json")
        tickets (read-decode "tickets.json")
        organisation-index (indexer/index "organisation" organisations users tickets)
        user-index (indexer/index "user" organisations users tickets)
        ticket-index (indexer/index "ticket" organisations users tickets)
        indexed-count {:organisations (count (:entities organisation-index))
                       :users         (count (:entities user-index))
                       :tickets       (count (:entities ticket-index))}]
    (log/debugf "Found %s organisations, %s users, %s tickets" (count organisations) (count users) (count tickets))
    (log/debugf "Will write indexed entities: %s" (pr-str indexed-count))
    (write-edn "organisation-index.edn" organisation-index)
    (write-edn "user-index.edn" user-index)
    (write-edn "ticket-index.edn" ticket-index)
    indexed-count))
