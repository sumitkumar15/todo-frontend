(ns todo-frontend.handler
  (:require-macros [hiccups.core :as hiccups :refer [html]]
                   [cljs.core.async.macros :refer [go]])
  (:require [todo-frontend.apicalls :as apis]
            [todo-frontend.templates :as tpl]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [goog.events :as events]
            [goog.dom :as dom]
            [goog.history.EventType :as EventType]))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn load-tasks
  [username]
  (go
    (let [resp (<! (apis/get-user-tasks username))]
      (html
        [:table [:tbody {:id "tasktable"} (apply str (map (fn [x] (tpl/tpl-task x)) resp))]]))))

(defn append-to-tasktable
  [^:Map task-map]
  (let [parent (dom/getElement "tasktable")
        childstr (tpl/tpl-task task-map)
        c-elem (let [d (dom/createElement "tr")]
                 (-> d (.setAttribute "id" (:_id task-map)))
                 (set-html! d childstr)
                 d)
        ]
    (dom/appendChild  parent c-elem)))

(defmulti dispatcher
          "Polymorphic function that handles the response based
          on :action of response"
          (fn [param tasks-state] (:action param :default)))

(defmethod dispatcher "newTask"
  [^:Map resp ^:atom tasks-state]
  (if (= (:status resp) "success")
    (append-to-tasktable (:data resp))
    nil))

(defn update-task-info
  [^:Map task-map]
  (let [elem (dom/getElement (:id task-map))
        allnodes (map #(-> % .-firstChild)
                      (array-seq (dom/getChildren elem)))]
    (set-html! (nth allnodes 1) (:title task-map))
    (set-html! (nth allnodes 2) (:desc task-map))))

(defmethod dispatcher "updateTask"
  [^:Map resp ^:Map params]
  (println resp)
  (if (= (:status resp) "success")
    (update-task-info params)
    (js/alert "Something went wrong")))

(defmethod dispatcher :default
  [])
