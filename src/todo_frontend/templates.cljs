(ns todo-frontend.templates
  ;(:use-macros [crate.def-macros :only [defpartial]])
  ;(:require [crate.core :as crate])
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [hiccups.runtime :as hiccupsrt]))

(defn load-test
  []
  (html [:p "Hey!"]))