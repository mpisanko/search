(ns mpisanko.index
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [mpisanko.indexing.entities :as entities]
            [mpisanko.indexing.organisation]
            [mpisanko.indexing.user])
  (:import (java.io IOException)))

(defn inverted-index
  "Create a naaive version of inverted index.
   No stemming or lemmatisation."
  [tokenise-fn index entity]
  (reduce (fn [index word]
            (update index (str/lower-case word) conj (:_id entity)))
          index
          (tokenise-fn entity)))

(defn- read-decode [file]
  (try
   (-> file
       slurp
       (json/decode true))
   (catch IOException e
     (log/errorf e "Exception reading file %s" file)
     (throw (ex-info (str "Problem reading file " file) {:problem "read-file"
                                                         :target file} e)))))

(defn- write-edn [file payload]
  (try
   (with-open [w (io/writer file)]
     (binding [*out* w]
       (pr payload)))
   (catch IOException e
     (log/errorf e "Exception writing index file %s. Please make sure that directory where you run application is writable and there is sufficient disk space." file)
     (throw (ex-info (str "Problem writing file " file) {:problem "write-file"
                                                         :target file} e)))))

(defn create
  "Create inverted indices for each of the entities (orgs, users, tickets)
   Indices also contain related entities"
  []
  (let [organisations (read-decode "organizations.json")
        users (read-decode "users.json")
        tickets (read-decode "tickets.json")
        organisation-index (entities/index "organisation" inverted-index organisations users tickets)
        user-index (entities/index "user" inverted-index organisations users tickets)
        indexed-count {:organisations (count (:entities organisation-index))
                       :users (count (:entities user-index))}]
    (log/debugf "Found %s organisations, %s users, %s tickets" (count organisations) (count users) (count tickets))
    (log/debugf "Will write indexed entities: %s" (pr-str indexed-count))
    (write-edn "organisation-index.edn" organisation-index)
    (write-edn "user-index.edn" user-index)
    indexed-count))
