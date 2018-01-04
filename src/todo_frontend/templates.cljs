(ns todo-frontend.templates
  ;(:use-macros [crate.def-macros :only [defpartial]])
  ;(:require [crate.core :as crate])
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

(defn tpl-task
  [^:Map task-map]
  (html
    [:tr {:id (:_id task-map) :class "row-elem"}
     [:td [:input {:type "checkbox" :class "chk-box"}]]
     [:td [:h3 {:class "title-btn"} (:title task-map)]]
     [:td [:p (:desc task-map)]]
     [:td [:button {:type "button" :value (:_id task-map) :class "delete-btn"} "Delete"]]]
    ;[:div {:id (:_id task-map) :class "task-layer"}]
    ))
