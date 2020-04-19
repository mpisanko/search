(ns mpisanko.search
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [mpisanko.index :as index]
            [clojure.string :as str])
  (:import (java.io File PushbackReader)))

(defn- read-index
  "Attempt to read an index for particular entity and if not found run indexer to create them"
  [entity]
  (let [index-file (str entity "-index.edn")]
    (when-not (.exists (File. index-file))
      (log/infof "Attempting to create index for %s" entity)
      (index/create))
    (with-open [r (PushbackReader. (io/reader index-file))]
      (edn/read r))))

(defn- find-empty [entities empty-field]
  (filter (fn [entity]
            (let [v (get entity empty-field)]
              (if (coll? v)
                (empty? v)
                (or (nil? v) (= "" v)))))
          (vals entities)))

(defn- find-entity [index entities arguments]
  (let [matched-ids (distinct (mapcat #(get index %) arguments))
        matches (vals (select-keys entities matched-ids))]
    matches))

(defn query
  "Perform a search given a query"
  [{:keys [organisation user ticket empty]} arguments]
  (let [entity (cond
                 organisation "organisation"
                 user "user"
                 ticket "ticket")
        {:keys [index entities]} (read-index entity)]
    (if empty
      (find-empty entities (first arguments))
      (find-entity index entities (map str/lower-case arguments)))))
