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
    [:div {:id (:_id task-map) :class "task-layer"}
     [:input {:type "checkbox" :class "chk-box"}
      [:h2 (:title task-map)]
      [:p (:desc task-map)]
      [:button {:type "button" :value (:_id task-map) } "Delete"]]
     ]))
