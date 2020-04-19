(ns mpisanko.application-test
  (:require [clojure.test :refer :all]
            [mpisanko.application :as app]
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

(def usage (:summary (cli/parse-opts ["-h"] app/cli-options)))

(deftest main-test-errors
  (with-redefs [app/show-help (fn [opts errs]
                         (reset! summary opts)
                         (reset! errors errs))
                i/create (fn []
                           (swap! index inc))]
    (testing "it prints help when requested"
      (app/-main "-h")
      (is (nil? @errors))
      (is (zero? @index))
      (is (= usage @summary)))

    (testing "it prints help when requested with long form"
      (app/-main "--help")
      (is (nil? @errors))
      (is (zero? @index))
      (is (= usage @summary)))

    (testing "it shows errors and prints help when invalid option used"
      (app/-main "--definitely-incorrect-option")
      (is (= ["Unknown option: \"--definitely-incorrect-option\""]
             @errors))
      (is (zero? @index))
      (is (= usage @summary)))

    (testing "it asks to specify entity/field which should be empty when searching by empty field"
      (app/-main "-e")
      (is (= [app/empty-field-error]
             @errors))
      (is (zero? @index))
      (is (= usage @summary)))))

(deftest main-test-empty
  (with-redefs [app/show-help (fn [opts errs]
                         (reset! summary opts)
                         (reset! errors errs))
                i/create (fn []
                           (swap! index inc))]
    (testing "it asks to specify entity/field which should be empty when searching by empty field"
      (app/-main "-e" "organisation" "details")
      (is (nil? @errors))
      (is (zero? @index))
      (is (= usage @summary)))))

(deftest main-test-search-no-term
  (with-redefs [app/show-help (fn [opts errs]
                                (reset! summary opts)
                                (reset! errors errs))
                i/create (fn []
                           (swap! index inc))]
    (testing "it asks to give a search term for org"
      (app/-main "-o")
      (is (= ["Please specify a search term for organisation"] @errors))
      (is (zero? @index))
      (is (= usage @summary)))

    (testing "it asks to give a search term for user"
      (app/-main "-u")
      (is (= ["Please specify a search term for user"] @errors))
      (is (zero? @index))
      (is (= usage @summary)))

    (testing "it asks to give a search term for ticket"
      (app/-main "-t")
      (is (= ["Please specify a search term for ticket"] @errors))
      (is (zero? @index))
      (is (= usage @summary)))))

(deftest main-test-index
  (with-redefs [app/show-help (fn [opts errs]
                         (reset! summary opts)
                         (reset! errors errs))
                i/create (fn []
                           (swap! index inc))]
    (testing "it asks to create indices when specified"
      (app/-main "-i")
      (is (nil? @errors))
      (is (nil? @summary))
      (is (pos? @index)))))