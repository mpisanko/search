(ns mpisanko.application
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mpisanko.index :as index]
            [mpisanko.search :as search]
            [mpisanko.presenter :as presenter])
  (:import (clojure.lang ExceptionInfo))
  (:gen-class))

(defn- show-help [summary errors]
  (when (seq errors)
    (println (str/join "\n" errors)))
  (println (str "Usage:\n" summary))
  (System/exit 1))

(def cli-options
  [["-h" "--help" "Print help information"]
   ["-i" "--index" "Create indices"]
   ["-o" "--organisation" "Query by organisation"]
   ["-u" "--user" "Query by user"]
   ["-t" "--ticket" "Query by ticket"]
   ["-e" "--empty" "Query for empty field specifying entity (one of the above flags) and field as argument, eg: '-u alias'"]])

(def empty-field-error "Please specify which entity (via flag) and field should be empty, eg: '-e -o details'")

(defn- no-search-argument [options]
  (let [entity (->> (filter options (keys options)) (map name) first)]
    (str "Please specify a search term for " entity)))

(defn- indexing-exception [e]
  (let [{:keys [problem target]} (ex-data e)]
    (log/errorf e "Problem while creating index: %s %s" problem target)))

(defn- valid-combination? [index organisation user ticket arguments]
  (let [selected-entities (count (filter identity [organisation user ticket]))]
    (or
      (and index (zero? selected-entities))
      (and (seq arguments) (= 1 selected-entities)))))

(defn- explain-usage [options summary errors]
  (let [{:keys [help empty]} options]
    (if (or help (seq errors))
      (show-help summary errors)
      (show-help summary [(if empty empty-field-error (no-search-argument options))]))))

(defn- create-indices []
  (try
    (let [indexed (index/create)]
      (println (str "Created indices of " (pr-str indexed))))
    (catch ExceptionInfo e
      (indexing-exception e))))

(defn- search [options arguments]
  (let [[results type] (search/query options arguments)
        matches (count results)
        message (str matches " " type (when-not (= 1 matches) "s")
                     " match your query\n")]
    (println (str message
                  (when (seq results)
                    (str
                      "\n"
                      (presenter/display type results)
                      "\n\n\n"
                      message))))))

(defn -main
  "Main entrypoint to the application. Parse command line options and dispatch to correct command"
  [& args]
  (let [{:keys [options summary arguments errors]} (cli/parse-opts args cli-options)
        {:keys [index organisation user ticket]} options]
    (if (valid-combination? index organisation user ticket arguments)
      (if index
        (create-indices)
        (search options arguments))
      (explain-usage options summary errors))))

