(ns mpisanko.index-test
  (:require [clojure.test :refer :all]
            [mpisanko.index :as i]
            [mpisanko.indexing.indexer :as indexer])
  (:import (clojure.lang ExceptionInfo)))

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
                   indexer/index (fn [e _ _ _]
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