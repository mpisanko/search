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

(defn -main
  "Main entrypoint to the application. Parse command line options and dispatch to correct command"
  [& args]
  (let [{:keys [options summary arguments errors]} (cli/parse-opts args cli-options)
        {:keys [help index empty organisation user ticket]} options]
    (cond
      (or help (seq errors))
          (show-help summary errors)
      (and (empty? arguments) empty (every? (complement true?) [organisation user ticket]))
          (show-help summary [empty-field-error])
      (and (empty? arguments) (or organisation user ticket))
          (show-help summary [(if empty empty-field-error (no-search-argument options))])
      index
          (try
            (let [indexed (index/create)]
              (println (str "Created indices of " (pr-str indexed))))
            (catch ExceptionInfo e
              (indexing-exception e)))
      (or organisation user ticket)
          (let [found (search/query options arguments)
                type (cond
                       organisation "organisation"
                       user         "user"
                       ticket       "ticket")]
            (println (str (count found)
                          " results match your query\n"
                          (when (seq found)
                            (presenter/display type found)))))
      :default
          (show-help summary errors))))

