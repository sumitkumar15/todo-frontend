(ns todo-frontend.handler
  (:require-macros [hiccups.core :as hiccups :refer [html]]
                   [cljs.core.async.macros :refer [go]])
  (:require [todo-frontend.apicalls :as apis]
            [todo-frontend.templates :as tpl]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn load-tasks
  [username]
  (go
    (let [resp (<! (apis/get-user-tasks username))]
      (html
        [:div (apply str (map (fn [x] (tpl/tpl-task x)) resp))]))))
