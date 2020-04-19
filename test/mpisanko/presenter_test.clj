(ns mpisanko.presenter-test
  (:require [clojure.test :refer :all]
            [mpisanko.presenter :as p]))

(def present-org (atom 0))
(def present-user (atom 0))
(def present-ticket (atom 0))

(deftest present-test
  (with-redefs [p/display-organisation (fn [_] (swap! present-org inc))
                p/display-user (fn [_] (swap! present-user inc))
                p/display-ticket (fn [_] (swap! present-ticket inc))]
   (testing "dispatches to display for organisation"
     (p/display "organisation" [1 2])
     (is (= 2 @present-org)))

   (testing "dispatches to display for users"
     (p/display "user" (range 10))
     (is (= 10 @present-user)))

   (testing "dispatches to display for tickets"
     (p/display "ticket" [1])
     (is (= 1 @present-ticket)))))
