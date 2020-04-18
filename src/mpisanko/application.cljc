(ns mpisanko.application
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [mpisanko.index :as index]
            [mpisanko.search :as search])
  (:gen-class))

(defn- help [summary errors]
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
   ["-e" "--empty" "Query for empty field specifying path to entity.field as argument, eg: 'user.alias'"]])

(def empty-field-error "Please specify which field of which entity should be empty, eg: organisation.description")

(defn -main
  "Main entrypoint to the application. Parse command line options and dispatch to correct command"
  [& args]
  (let [{:keys [options summary arguments errors]} (cli/parse-opts args cli-options)]
    (cond
      (or (:help options) errors) (help summary errors)
      (and (:empty options) (empty? arguments)) (help summary [empty-field-error])
      (:index options) (index/create)
      :default (search/query options arguments))))

