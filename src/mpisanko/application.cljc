(ns mpisanko.application
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mpisanko.index :as index]
            [mpisanko.search :as search])
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
   ["-e" "--empty" "Query for empty field specifying path to entity.field as argument, eg: 'user alias'"]])

(def empty-field-error "Please specify which entity and field should be empty, eg: 'organisation details'")

(defn- no-search-argument [options]
  (let [entity (->> (filter options (keys options)) (map name) first)]
    (str "Please specify a search term for " entity)))

(defn- indexing-exception [e]
  (let [{:keys [problem target]} (ex-data e)]
   (log/errorf e "Problem while creating index: %s %s" problem target)))

(defn -main
  "Main entrypoint to the application. Parse command line options and dispatch to correct command"
  [& args]
  (let [{:keys [options summary arguments errors]} (cli/parse-opts args cli-options)
        {:keys [help index empty organisation user ticket]} options]
    (cond
      (or help (seq errors))
          (show-help summary errors)
      (and (empty? arguments) empty)
          (show-help summary [empty-field-error])
      (and (empty? arguments) (or organisation user ticket))
          (show-help summary [(no-search-argument options)])
      index
          (try
            (let [indexed (index/create)]
              (println (str "Created indices of " (pr-str indexed))))
            (catch ExceptionInfo e
              (indexing-exception e)))
      (or organisation user ticket)
          (let [found (search/query options arguments)]
            (println (count found) "results matching your query:\n" (pr-str found)))
      :default
          (show-help summary errors))))

