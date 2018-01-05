(ns todo-frontend.templates
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [hiccups.runtime :as hiccupsrt]))

(defn load-test
  []
  (html [:p "Hey!"]))

(defn tpl-notfound
  []
  (html
    [:div {:class "notfound"}
     "Page not found"]))

(defn check?
  [s]
  (if (= s "C")
    true
    false))

(defn tpl-task
  [^:Map task-map]
  (html
    [:tr {:id (:_id task-map) :class "row-elem"}
     [:td [:input {:type "checkbox"
                   :class "chk-box"
                   :value (if (check? (:status task-map)) "on" "off")
                   :checked (check? (:status task-map))}]]
     [:td [:h3 (:title task-map)]]
     [:td [:p (:desc task-map)]]
     [:td [:button {:type "button" :value (:_id task-map) :class "delete-btn"}
           [:i {:class "material-icons"} "delete"]]]]
    ;[:div {:id (:_id task-map) :class "task-layer"}]
    ))
