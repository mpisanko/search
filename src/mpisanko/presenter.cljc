(ns mpisanko.presenter
  (:require [clojure.string :as str]))

(defmulti display (fn [type _entities] type))

(declare display-organisation)
(declare display-user)

(defn- print-fields
  ([fields-labels o]
   (print-fields fields-labels o identity))
  ([fields-labels o value-fn]
   (str/join "\n"
             (for [[field label] fields-labels]
               (let [title (or label (name field))
                     tabs (Math/ceil (/ (- (* 8 2) (inc (count title))) 8))]
                 (str title ":" (apply str (repeat tabs "\t")) (value-fn (get o field))))))))

(defn display-ticket [{:keys [organisation submitter assignee]
                       :as   ticket}]
  (str
    (print-fields [[:subject "Ticket"]
                   [:description]
                   [:url]
                   [:type]
                   [:priority]
                   [:status]
                   [:via]
                   [:created_at "created"]
                   [:due_at "due"]
                   [:has_incidents "incidents"]]
                  ticket)
    "\n"
    (print-fields [[:tags]] ticket (partial str/join ", "))
    (when organisation
      (str
        "\n\n-------------------  organisation: ------------------- \n\n"
        (display-organisation organisation)))
    (when submitter
      (str
        "\n\n-------------------  submitter: -------------------\n\n"
        (display-user submitter)))
    (when assignee
      (str
        "\n\n-------------------  assignee: -------------------\n\n"
        (display-user assignee)))))

(defn display-user [{:keys [organisation tickets]
                     :as   user}]
  (str
    (print-fields [[:name "User"]
                   [:alias]
                   [:url]
                   [:email]
                   [:phone]
                   [:role]
                   [:signature]
                   [:created_at "created"]
                   [:last_login_at "last login"]
                   [:timezone]]
                  user)
    "\n"
    (print-fields [[:tags]] user (partial str/join ", "))
    (when organisation
      (str
        "\n\n-------------------  organisation: ------------------- \n\n"
        (display-organisation organisation)))
    (when (seq tickets)
      (str
        "\n\n-------------------  tickets: -------------------\n\n"
        (display "ticket" tickets)))))

(defn display-organisation [{:keys [users tickets]
                             :as   organisation}]
  (str
    (print-fields [[:name "Organisation"]
                   [:details]
                   [:url]
                   [:created_at "created"]]
                  organisation)
    "\n"
    (print-fields [[:domain_names "domain names"]
                   [:tags]]
                  organisation
                  (partial str/join ", "))
       (when (seq users)
         (str
           "\n\n-------------------  users:   -------------------\n\n"
           (display "user" users)))
       (when (seq tickets)
         (str
           "\n\n-------------------  tickets: -------------------\n\n"
           (display "ticket" tickets)))))

(defmethod display "organisation" [_ entities]
  (str/join "\n\n======================================================================================\n\n"
            (map display-organisation entities)))

(defmethod display "user" [_ entities]
  (str/join "\n\n---------------\n"
            (map display-user entities)))

(defmethod display "ticket" [_ entities]
  (str/join "\n\n---------------\n"
            (map display-ticket entities)))