(ns mpisanko.presenter
  (:require [clojure.string :as str]))

(defmulti display (fn [type _entities] type))

(defn display-ticket [{:keys [_id url created_at type subject description priority
                              status tags has_incidents due_at via]}]
  (str "Ticket:\t\t" subject
       "\ndescription:\t" description
       "\nurl:\t\t" url
       "\ntype:\t\t" type
       "\npriority:\t" priority
       "\nstatus:\t\t" status
       "\nvia:\t\t" via
       "\ncreated:\t" created_at
       "\ndue:\t\t" due_at
       "\nincidents:\t" has_incidents
       "\ntags:\t\t" (str/join ", " tags)))

(defn display-user [{:keys [url name alias created_at
                            timezone last_login_at email phone signature
                            tags role]}]
  (str "User:\t\t" name
       "\nalias:\t\t" alias
       "\nurl:\t\t" url
       "\nemail:\t\t" email
       "\nphone:\t\t" phone
       "\nrole:\t\t" role
       "\nsignature:\t" signature
       "\ncreated:\t" created_at
       "\nlast login:\t" last_login_at
       "\ntimezone:\t" timezone
       "\ntags:\t\t" (str/join ", " tags)))

(defn display-organisation [{:keys [_id url name domain_names created_at details tags users tickets]}]
  (str "Organisation:\t" name
       "\ndetails:\t" details
       "\nurl:\t\t" url
       "\ncreated:\t" created_at
       "\ndomain names:\t" (str/join ", " domain_names)
       "\ntags:\t\t" (str/join ", " tags)
       "\n\n-------------------  users:   -------------------\n\n"
       (display "user" users)
       "\n\n-------------------  tickets: -------------------\n\n"
       (display "ticket" tickets)))

(defmethod display "organisation" [_ entities]
  (str/join "\n\n======================================================================================\n\n"
            (map display-organisation entities)))

(defmethod display "user" [_ entities]
  (str/join "\n\n---------------\n"
            (map display-user entities)))

(defmethod display "ticket" [_ entities]
  (str/join "\n\n---------------\n"
            (map display-ticket entities)))