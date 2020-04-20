(ns mpisanko.index-test
  (:require [clojure.test :refer :all]
            [mpisanko.index :as i]
            [mpisanko.indexing.entities :as e])
  (:import (java.io File)
           (clojure.lang ExceptionInfo)))

(deftest inverted-index-test
  (let [indexer (partial i/inverted-index :words)
        inverted-index (-> {}
                           (indexer {:words ["a" "b" "c"] :_id 666})
                           (indexer {:words ["c" "C" "D"] :_id 667}))]
    (is (= {"a" #{666}
            "b" #{666}
            "c" #{666 667}
            "d" #{667}}
           (reduce-kv (fn [acc k v] (assoc acc k (set v))) {} inverted-index)))))

(deftest write-edn-test
  (testing "when we can write and there is enough space"
    (let [f "test/test.edn"
          payload {:a 1}]
      (#'i/write-edn f payload)
      (let [c (read-string (slurp f))]
        (is (= payload c)))
      (.delete (File. f))))

  (testing "when there is a problem writing to file"
    (is (thrown? ExceptionInfo
                 (#'i/write-edn "/not-there/foo" {})))))

(deftest read-decode-test
  (testing "it reads in a JSON file and decodes to a sequence of maps"
    (let [orgs (#'i/read-decode "organizations.json")]
      (is (coll? orgs))
      (is (= 25 (count orgs)))))

  (testing "when the file does not exist"
    (is (thrown? ExceptionInfo
                 (#'i/read-decode "foobar-not-there")))))

(deftest index-test
  (testing "correct files are used as input, output and multimethod dispatches to correct entities"
   (let [read-files (atom [])
         written-files (atom [])
         index-calls (atom [])]
     (with-redefs [i/read-decode (fn [file]
                                   (swap! read-files conj file)
                                   [])
                   i/write-edn (fn [file _]
                                 (swap! written-files conj file))
                   e/index (fn [e _ _ _ _]
                             (swap! index-calls conj e)
                             {})]
       (i/create)
       (is (= ["organizations.json"
               "users.json"
               "tickets.json"]
              @read-files))
       (is (= ["organisation-index.edn"
               "user-index.edn"]
              @written-files))
       (is (= ["organisation"
               "user"]
              @index-calls))))))