(ns mpisanko.indexing.user-test
  (:require [clojure.test :refer :all]
            [mpisanko.indexing.user :as u]
            [clojure.string :as str]))

(deftest tokens-test
  (testing "it splits selected fields on space"
    (let [user {:role "agent",
                :tags ["Veguita" "Navarre" "Elizaville" "Beaulieu"],
                :_id 75,
                :email "rosannasimpson@flotonic.com",
                :timezone "US Minor Outlying Islands",
                :last_login_at "2012-10-15T12:36:41 -11:00",
                :organization_id 119,
                :locale "zh-CN",
                :phone "8615-883-099",
                :name "Catalina Simpson",
                :signature "Don't Worry Be Happy!",
                :suspended true,
                :alias "Miss Rosanna",
                :external_id "0db0c1da-8901-4dc3-a469-fe4b500d0fca",
                :active false,
                :url "http://initech.zendesk.com/api/v2/users/75.json",
                :shared true,
                :verified true,
                :created_at "2016-06-07T09:18:00 -10:00"}
          tokens (vec (u/tokens user))
          created (first (str/split (:created_at user) #"\s"))
          last-login (first (str/split (:last_login_at user) #"\s"))]
      (is (pos? (count tokens)))
      (is (some #(= % created) tokens))
      (is (some #(= % last-login) tokens))
      (doseq [k [:external_id :email :phone :locale
                 :organization_id :role]]
        (is (some #(= % (get user k)) tokens)))
      (doseq [n (str/split (:name user) #"\s")]
       (is (some #(= % n) tokens)))
      (doseq [s (str/split (:signature user) #"\s")]
        (is (some #(= % s) tokens)))
      (doseq [a (str/split (:alias user) #"\s")]
       (is (some #(= % a) tokens)))
      (doseq [t (str/split (:timezone user) #"\s")]
        (is (some #(= % t) tokens)))
      (doseq [t (:tags user)]
        (is (some #(= % t) tokens))))))

(deftest enrich-test
  (testing "groups users by _id, associates organisation and tickets"
    (let [organisations [{:_id 1} {:_id 2} {:_id 3}]
          users [{:_id 1 :organization_id 1}
                 {:_id 2 :organization_id 1}
                 {:_id 3 :organization_id 2}]
          tickets [{:_id 1 :organization_id 1 :submitter_id 1}
                   {:_id 2 :organization_id 1 :assignee_id 2}
                   {:_id 3 :organization_id 1 :assignee_id 1}
                   {:_id 4 :organization_id 2 :submitter_id 3}
                   {:_id 5 :organization_id 2 :assignee_id 3}]
          enriched (u/enrich organisations users tickets)]
      (is (= 3 (count enriched)))
      (is (= 2 (count (get-in enriched [3 :tickets]))))
      (is (= 2 (count (get-in enriched [1 :tickets]))))
      (is (= 1 (count (get-in enriched [2 :tickets]))))
      (is (= {:_id 1} (get-in enriched [1 :organisation])))
      (is (= {:_id 1} (get-in enriched [2 :organisation])))
      (is (= {:_id 2} (get-in enriched [3 :organisation]))))))