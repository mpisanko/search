(ns mpisanko.indexing.organisation
  (:require [mpisanko.indexing.indexer :as indexer]))

(defn tokens
  "Basic tokenisation function - only using selected words"
  [{:keys [name details tags domain_names external_id created_at]}]
  (conj
    (mapcat indexer/words
            (conj
              (into tags domain_names)
              name details))
    external_id
    (first (indexer/words created_at))))

(defn enrich
  "Map organisations by their ID and associate related entities onto each organisation"
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

(defmethod indexer/tokeniser-enricher-entities "organisation" [_ organisations _users _tickets]
  [tokens enrich organisations])