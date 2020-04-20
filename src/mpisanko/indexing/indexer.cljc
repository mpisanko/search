(ns mpisanko.indexing.indexer
  (:require [clojure.string :as str]))

(defn words [s]
  (when s
    (str/split s #"\s+")))

(defn inverted-index
  "Create a naaive version of inverted index.
   No stemming or lemmatisation."
  [tokenise-fn index entity]
  (reduce (fn [index word]
            (if word
              (update index (str/lower-case word) conj (:_id entity))
              index))
          index
          (tokenise-fn entity)))

(defmulti tokeniser-enricher-entities (fn [entity _organisations _users _tickets] entity))

(defn index
  "Creates inverted index for given entity including related entities"
  [entity organisations users tickets]
  (let [[tokens-fn enrich-fn entities] (tokeniser-enricher-entities
                                         entity
                                         organisations
                                         users
                                         tickets)]
    {:index    (reduce (partial inverted-index tokens-fn)
                       {}
                       entities)
     :entities (enrich-fn organisations users tickets)
     :type     entity}))