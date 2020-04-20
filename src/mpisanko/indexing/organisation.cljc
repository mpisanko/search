(ns mpisanko.indexing.organisation
  (:require [clojure.string :as str]))

(defn- words [s]
  (str/split s #"\s+"))

(defn tokens
  "Basic tokenisation function - only using selected words"
  [{:keys [name details tags domain_names external_id created_at]}]
  (concat (words name)
          (words details)
          (mapcat words tags)
          (mapcat words domain_names)
          [external_id]
          (drop-last (words created_at))))

(defn enrich
  "Group organisations by their ID and associate related entities onto each organisation"
  [organisations users tickets]
  (let [users-by-org (group-by :organization_id users)
        tickets-by-org (group-by :organization_id tickets)
        organisations-by-id (group-by :_id organisations)]
    (reduce-kv
      (fn [acc k v]
        (let [enriched (assoc (first v)
                         :users (get users-by-org k)
                         :tickets (get tickets-by-org k))]
          (assoc acc k enriched)))
      {}
      organisations-by-id)))

(defn index [index-fn organisations users tickets]
  (let [index (reduce (partial index-fn tokens)
                      {}
                      organisations)
        enriched (enrich organisations users tickets)]
    {:index index
     :entities enriched
     :type "organisation"}))