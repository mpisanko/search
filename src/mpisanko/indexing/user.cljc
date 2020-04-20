(ns mpisanko.indexing.user
  (:require [clojure.string :as str]))

(defn- words [s]
  (str/split s #"\s+"))

(defn tokens
  "Basic tokenisation function - only using selected words"
  [{:keys [name alias tags role external_id created_at last_login_at
           locale timezone email phone signature organization_id]}]
  (concat (words name)
          (words alias)
          (mapcat words tags)
          (words role)
          [external_id]
          [locale]
          [email]
          [phone]
          [organization_id]
          (words signature)
          (words timezone)
          (drop-last (words created_at))
          (drop-last (words last_login_at))))

(defn enrich
  "Group users by their ID and associate related entities onto each user"
  [organisations users tickets]
  (let [users-by-id (group-by :_id users)
        tickets-by-submitter (group-by :submitter_id tickets)
        tickets-by-assignee (group-by :assignee_id tickets)]
    (reduce-kv
      (fn [acc k v]
        (let [user (first v)
              organisation (reduce (fn [_ o] (when (= (:_id o)
                                                      (:organization_id user))
                                               (reduced o)))
                                   nil
                                   organisations)
              enriched (assoc user
                         :organisation organisation
                         :tickets (concat (get tickets-by-assignee k)
                                          (get tickets-by-submitter k)))]
          (assoc acc k enriched)))
      {}
      users-by-id)))

(defn index [index-fn organisations users tickets]
  (let [index (reduce (partial index-fn tokens)
                      {}
                      users)
        enriched (enrich organisations users tickets)]
    {:index index
     :entities enriched
     :type "user"}))