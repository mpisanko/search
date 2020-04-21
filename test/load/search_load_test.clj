(ns load.search-load-test
  (:require [clojure.test :refer :all]
            [mpisanko.search :as s]
            [criterium.core :as criterium]
            [clojure.string :as str]))

(deftest ^:load query-test
  (let [arguments (str/split (or (System/getenv "ARGS") "artisan geekfarm")
                             #"\s+")
        empty (try
                (Boolean/parseBoolean (or (System/getenv "EMPTY") "false"))
                (catch Exception _
                  false))]

    (println "Running load test for organisation, empty:" empty " with arguments" arguments)
    (criterium/quick-bench (s/query {:organisation true :empty empty} arguments))

    (println "Running load test for user, empty:" empty " with arguments" arguments)
    (criterium/quick-bench (s/query {:user true :empty empty} arguments))

    (println "Running load test for ticket, empty:" empty " with arguments" arguments)
    (criterium/quick-bench (s/query {:ticket true :empty empty} arguments))))

