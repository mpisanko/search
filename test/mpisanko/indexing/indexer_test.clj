(ns mpisanko.indexing.indexer-test
  (:require [clojure.test :refer :all]
            [mpisanko.indexing.indexer :as i]))

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