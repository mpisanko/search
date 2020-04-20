(ns mpisanko.indexing.user
  (:require [mpisanko.indexing.indexer :as indexer]))

(defn tokens
  "Basic tokenisation function - only using selected words"
  [{:keys [name alias tags role external_id created_at last_login_at
           locale timezone email phone signature organization_id]}]
  (conj
    (mapcat indexer/words
            (into tags [name alias signature timezone]))
    role
    email
    phone
    locale
    external_id
    organization_id
    (first (indexer/words created_at))
    (first (indexer/words last_login_at))))

(defn enrich
  "Group users by their ID and associate related entities onto each user"
  [organisations users tickets]
  (let [users-by-id (group-by :_id users)
        organisations-by-id (group-by :_id organisations)
        tickets-by-submitter (group-by :submitter_id tickets)
        tickets-by-assignee (group-by :assignee_id tickets)]
    (reduce-kv
      (fn [acc k v]
        (let [user (first v)
              enriched (assoc user
                         :organisation (first (get organisations-by-id (:organization_id user)))
                         :tickets (concat (get tickets-by-assignee k)
                                          (get tickets-by-submitter k)))]
          (assoc acc k enriched)))
      {}
      users-by-id)))

(defmethod indexer/tokeniser-enricher-entities "user" [_ _organisations users _tickets]
  [tokens enrich users])