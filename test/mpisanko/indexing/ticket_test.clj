(ns mpisanko.indexing.ticket-test
  (:require [clojure.test :refer :all]
            [mpisanko.indexing.ticket :as t]
            [clojure.string :as str]))

(deftest tokens-test
  (testing "it splits selected fields on space"
    (let [ticket {:description     "Esse nisi occaecat pariatur veniam culpa dolore anim elit aliquip. Cupidatat mollit nulla consectetur ullamco tempor esse.",
                  :tags            ["Georgia" "Tennessee" "Mississippi" "Marshall Islands"],
                  :_id             "50dfc8bc-31de-411e-92bf-a6d6b9dfa490",
                  :submitter_id    43,
                  :organization_id 103,
                  :via             "voice",
                  :type            "task",
                  :priority        "high",
                  :assignee_id     54,
                  :status          "hold",
                  :external_id     "8bc8bee7-2d98-4b69-b4a9-4f348ff41fa3",
                  :url             "http://initech.zendesk.com/api/v2/tickets/50dfc8bc-31de-411e-92bf-a6d6b9dfa490.json",
                  :due_at          "2016-08-03T09:17:37 -10:00",
                  :has_incidents   true,
                  :subject         "A Problem in South Africa",
                  :created_at      "2016-03-08T09:44:54 -11:00"}
          tokens (vec (t/tokens ticket))
          created (first (str/split (:created_at ticket) #"\s"))
          due (first (str/split (:due_at ticket) #"\s"))]

      (is (pos? (count tokens)))
      (is (some #(= % created) tokens))
      (is (some #(= % due) tokens))
      (doseq [k [:via :type :status :external_id :priority]]
        (is (some #(= % (get ticket k)) tokens)))
      (doseq [t (mapcat #(str/split % #"\s") (:tags ticket))]
        (is (some #(= % t) tokens)))
      (doseq [d (str/split (:description ticket) #"\s")]
        (is (some #(= % d) tokens)))
      (doseq [s (str/split (:subject ticket) #"\s")]
        (is (some #(= % s) tokens))))))

(deftest enrich-test
  (testing "maps tickets by _id associates organisation, submitter and assignee"
    (let [organisations [{:_id 1} {:_id 2} {:_id 3}]
          users [{:_id 1 :organization_id 1}
                 {:_id 2 :organization_id 1}
                 {:_id 3 :organization_id 2}
                 {:_id 4 :organization_id 3}]
          tickets [{:_id 1 :organization_id 1 :submitter_id 1 :assignee_id 2}
                   {:_id 2 :organization_id 1 :assignee_id 2 :submitter_id 1}
                   {:_id 3 :organization_id 1 :assignee_id 1}
                   {:_id 4 :organization_id 2 :submitter_id 3}
                   {:_id 5 :organization_id 3 :assignee_id 4}]
          enriched (t/enrich organisations users tickets)]
      (is (= 5 (count enriched)))
      (is (= {:_id             3
              :organization_id 1
              :assignee_id     1
              :organisation    {:_id 1}
              :assignee        {:_id 1 :organization_id 1}
              :submitter       nil}
             (get enriched 3)))
      (is (= {:_id             2
              :organization_id 1
              :assignee_id     2
              :submitter_id    1
              :organisation    {:_id 1}
              :submitter       {:_id 1 :organization_id 1}
              :assignee        {:_id 2 :organization_id 1}}
             (get enriched 2))))))