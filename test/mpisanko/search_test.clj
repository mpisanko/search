(ns mpisanko.search-test
  (:require [clojure.test :refer :all]
            [mpisanko.search :as s]))

(def entities {1 {:_id 1 :name "abc" :details "foo" :tags ["666" "667"]}
               2 {:_id 2 :name "def" :details "bar" :tags []}
               3 {:_id 3 :name "ghi" :details "foo bar" :tags ["42"]}})

(def index {"abc" [1]
            "def" [2]
            "ghi" [3]
            "foo" [1 3]
            "bar" [2 3]})

(deftest find-entity-test
  (testing "searching with single keyword"
    (let [matches (#'s/find-entity index entities ["ABC"])]
      (is (= 1 (count matches)))
      (is (= [{:_id 1 :name "abc" :details "foo" :tags ["666" "667"]}] matches))))

  (testing "searching with multiple keywords"
    (let [matches (#'s/find-entity index entities ["ABC" "FOO"])]
      (is (= 2 (count matches)))
      (is (= [{:_id 1 :name "abc" :details "foo" :tags ["666" "667"]}
              {:_id 3 :name "ghi" :details "foo bar" :tags ["42"]}]
             matches)))))

(deftest find-empty-test
  (let [matches (#'s/find-empty entities "tags")]
    (is (= 1 (count matches)))
    (is (= [{:_id 2 :name "def" :details "bar" :tags []}] matches)))

  (testing "returns all entities when there is no field specified"
    (is (= 3 (count (#'s/find-empty entities "foo")))))

  (testing "returns none if all entities have the field not empty"
    (is (empty? (#'s/find-empty entities "_id")))))
