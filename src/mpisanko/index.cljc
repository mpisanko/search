(ns mpisanko.index
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [mpisanko.indexing.organisation :as organisation])
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
        organisation-index (organisation/index inverted-index organisations users tickets)
        indexed-count {:organisations (count (:entities organisation-index))}]
    (log/debugf "Will write indexed organisations: %s" indexed-count)
    (write-edn "organisation-index.edn" organisation-index)
    indexed-count))
