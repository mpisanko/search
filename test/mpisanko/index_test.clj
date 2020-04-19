(ns mpisanko.index-test
  (:require [clojure.test :refer :all]
            [mpisanko.index :as i])
  (:import (java.io File)
           (clojure.lang ExceptionInfo)))

(deftest inverted-index-test
  (let [indexer (partial i/inverted-index :words)
        inverted-index (-> {}
                           (indexer {:words ["a" "b" "c"] :_id 666})
                           (indexer {:words ["c" "C" "d"] :_id 667}))]
    (is (= {"a" #{666}
            "b" #{666}
            "c" #{666 667}
            "C" #{667}
            "d" #{667}}
           (reduce-kv (fn [acc k v] (assoc acc k (set v))) {} inverted-index)))))

(deftest write-edn-test
  (testing "when we can write and there is enough space"
    (let [f "test/test.edn"
          payload {:a 1}]
      (#'mpisanko.index/write-edn f payload)
      (let [c (read-string (slurp f))]
        (is (= payload c)))
      (.delete (File. f))))

  (testing "when there is a problem writing to file"
    (is (thrown? ExceptionInfo
                 (#'mpisanko.index/write-edn "/not-there/foo" {})))))

(deftest read-decode-test
  (testing "it reads in a JSON file and decodes to a sequence of maps"
    (let [orgs (#'mpisanko.index/read-decode "organizations.json")]
      (is (coll? orgs))
      (is (= 25 (count orgs)))))

  (testing "when the file does not exist"
    (is (thrown? ExceptionInfo
                 (#'mpisanko.index/read-decode "foobar-not-there")))))