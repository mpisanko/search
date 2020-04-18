(ns mpisanko.search-test
  (:require [clojure.test :refer :all]
            [mpisanko.search :as s]
            [mpisanko.index :as i]
            [clojure.tools.cli :as cli]))

(def summary (atom {}))
(def errors (atom {}))
(def index (atom 0))

(use-fixtures :each (fn [f]
                      (reset! summary nil)
                      (reset! errors nil)
                      (reset! index 0)
                      (f)))

(def usage (:summary (cli/parse-opts ["-h"] s/cli-options)))

(deftest main-test-errors
  (with-redefs [s/help (fn [opts errs]
                         (reset! summary opts)
                         (reset! errors errs))
                i/create (fn []
                           (swap! index inc))]
    (testing "it prints help when requested"
      (s/-main "-h")
      (is (nil? @errors))
      (is (zero? @index))
      (is (= usage @summary)))

    (testing "it prints help when requested with long form"
      (s/-main "--help")
      (is (nil? @errors))
      (is (zero? @index))
      (is (= usage @summary)))

    (testing "it shows errors and prints help when invalid option used"
      (s/-main "--definitely-incorrect-option")
      (is (= ["Unknown option: \"--definitely-incorrect-option\""]
             @errors))
      (is (zero? @index))
      (is (= usage @summary)))

    (testing "it asks to specify entity/field which should be empty when searching by empty field"
      (s/-main "-e")
      (is (= [s/empty-field-error]
             @errors))
      (is (zero? @index))
      (is (= usage @summary)))))

(deftest main-test-empty
  (with-redefs [s/help (fn [opts errs]
                         (reset! summary opts)
                         (reset! errors errs))
                i/create (fn []
                           (swap! index inc))]
    (testing "it asks to specify entity/field which should be empty when searching by empty field"
      (s/-main "-e" "org.name" "foobar")
      (is (nil? @errors))
      (is (zero? @index))
      (is (nil? @summary)))))

(deftest main-test-index
  (with-redefs [s/help (fn [opts errs]
                         (reset! summary opts)
                         (reset! errors errs))
                i/create (fn []
                           (swap! index inc))]
    (testing "it asks to create indices when specified"
      (s/-main "-i")
      (is (nil? @errors))
      (is (nil? @summary))
      (is (pos? @index)))))