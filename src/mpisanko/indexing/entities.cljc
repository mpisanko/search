(ns mpisanko.indexing.entities)

(defmulti index (fn [entity _index-fn _organisations _users _tickets] entity))
