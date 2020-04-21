(ns mpisanko.presenter-test
  (:require [clojure.test :refer :all]
            [mpisanko.presenter :as p]
            [clojure.string :as str]))

(def present-organisation (atom 0))
(def present-user (atom 0))
(def present-ticket (atom 0))

(deftest present-test
  (with-redefs [p/display-organisation (fn [_] (swap! present-organisation inc))
                p/display-user (fn [_] (swap! present-user inc))
                p/display-ticket (fn [_] (swap! present-ticket inc))]
   (testing "dispatches to display for organisation"
     (p/display "organisation" [1 2])
     (is (= 2 @present-organisation)))

   (testing "dispatches to display for users"
     (p/display "user" (range 10))
     (is (= 10 @present-user)))

   (testing "dispatches to display for tickets"
     (p/display "ticket" [1])
     (is (= 1 @present-ticket)))))

(deftest print-fields-test
  (testing "it pads labels with tabs to align values"
    (let [o {:name "name" :description "desc" :created_at "yesterday"}
          string (#'p/print-fields
                   [[:name]
                    [:description]
                    [:created_at "created"]]
                   o)]
      (is (= "name:\t\tname\ndescription:\tdesc\ncreated:\tyesterday"
             string))))

  (testing "with applies the supplied function"
    (let [o {:name "NAME" :description "DESC" :created_at "TODAY"}
          string (#'p/print-fields
                   [[:name]
                    [:description]
                    [:created_at "created"]]
                   o
                   str/lower-case)]
      (is (= "name:\t\tname\ndescription:\tdesc\ncreated:\ttoday"
             string)))))
