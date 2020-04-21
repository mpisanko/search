(ns mpisanko.indexing.ticket
  (:require [mpisanko.indexing.indexer :as indexer]))

(defn tokens
  "Basic tokenisation function - only using selected words"
  [{:keys [external_id type subject description created_at due_at priority
           status submitter_id assignee_id organization_id tags via]}]
  (conj
    (mapcat indexer/words
            (into tags [subject description]))
    external_id
    type
    priority
    via
    status
    submitter_id
    assignee_id
    organization_id
    (first (indexer/words created_at))
    (first (indexer/words due_at))))

(defn enrich
  "Map tickets by their ID and associate related entities onto each ticket"
  [organisations users tickets]
  (let [users-by-id (group-by :_id users)
        organisations-by-id (group-by :_id organisations)]
    (reduce
      (fn [acc ticket]
        (let [enriched (assoc ticket
                         :organisation (first (get organisations-by-id (:organization_id ticket)))
                         :submitter (first (get users-by-id (:submitter_id ticket)))
                         :assignee (first (get users-by-id (:assignee_id ticket))))]
          (assoc acc (:_id ticket) enriched)))
      {}
      tickets)))

(defmethod indexer/tokeniser-enricher-entities "ticket" [_ _organisations _users tickets]
  [tokens enrich tickets])