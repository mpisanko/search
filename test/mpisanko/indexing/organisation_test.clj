(ns mpisanko.indexing.organisation-test
  (:require [clojure.test :refer :all]
            [mpisanko.indexing.organisation :as o]
            [clojure.string :as str]))

(deftest tokens-test
  (testing "it splits selected fields on space"
    (let [org {:shared_tickets false,
               :tags ["Vance" "Ray" "Jacobs" "Frank"],
               :_id 125,
               :name "Strezzö",
               :details "MegaCorp",
               :external_id "42a1a845-70cf-40ed-a762-acb27fd606cc",
               :url "http://initech.zendesk.com/api/v2/organizations/125.json",
               :created_at "2016-02-21T06:11:51 -11:00",
               :domain_names ["techtrix.com" "teraprene.com" "corpulse.com" "flotonic.com"]}
          tokens (vec (o/tokens org))
          created (first (str/split (:created_at org) #"\s"))]
      (is (pos? (count tokens)))
      (is (some #(= % created) tokens))
      (is (some #(= % (:external_id org)) tokens))
      (is (some #(= % (:name org)) tokens))
      (is (some #(= % (:details org)) tokens))
      (doseq [t (:tags org)]
        (is (some #(= % t) tokens)))
      (doseq [d (:domain_names org)]
        (is (some #(= % d) tokens))))))

(deftest enrich-test
  (testing "groups organisations by _id and associates users and tickets that refer them by organization_id"
    (let [orgs [{:_id 1} {:_id 2} {:_id 3}]
          users [{:_id 1 :organization_id 1} {:_id 2 :organization_id 1} {:_id 3 :organization_id 2}]
          tickets [{:_id 1 :organization_id 1} {:_id 2 :organization_id 1} {:_id 3 :organization_id 1}
                   {:_id 4 :organization_id 3} {:_id 5 :organization_id 3}]
          enriched (o/enrich orgs users tickets)]
      (is (= 3 (count enriched)))
      (is (= 2 (count (get-in enriched [1 :users]))))
      (is (= 1 (count (get-in enriched [2 :users]))))
      (is (zero? (count (get-in enriched [3 :users]))))
      (is (= 3 (count (get-in enriched [1 :tickets]))))
      (is (= 2 (count (get-in enriched [3 :tickets]))))
      (is (zero? (count (get-in enriched [2 :tickets])))))))

