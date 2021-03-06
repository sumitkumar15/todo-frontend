(ns todo-frontend.handler
  (:require-macros [hiccups.core :as hiccups :refer [html]]
                   [cljs.core.async.macros :refer [go]])
  (:require [todo-frontend.apicalls :as apis]
            [todo-frontend.templates :as tpl]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [goog.events :as events]
            [goog.dom :as dom]
            [goog.history.EventType :as EventType]
            [todo-frontend.eventmanager :as evm]))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn load-tasks
  [username]
  (go
    (let [resp (<! (apis/get-user-tasks username))]
      (if (= "success" (:status resp))
        (html
          [:table [:tbody {:id "tasktable"}
                   (apply str (map (fn [x] (tpl/tpl-task x)) (:data resp)))]])
        (html [:h3 "You need to register first"]))
      )))

(defn append-to-tasktable
  [^:Map task-map user]
  (let [parent (dom/getElement "tasktable")
        childstr (tpl/tpl-task task-map)
        c-elem (let [d (dom/createElement "tr")]
                 (-> d (.setAttribute "id" (:_id task-map)))
                 (set-html! d childstr)
                 d)
        children (map #(-> % .-firstChild)
                      (array-seq (dom/getChildren c-elem)))
        ]
    (evm/listen-modify c-elem user)
    (evm/listen-completed (nth children 0) user)
    (evm/listen-delete (nth children 3) user)
    (dom/appendChild  parent c-elem)))

(defmulti dispatcher
          "Polymorphic function that handles the response based
          on :action of response"
          (fn [param user] (:action param :default)))

(defmethod dispatcher "newTask"
  [^:Map resp user]
  (if (= (:status resp) "success")
    (append-to-tasktable (:data resp) user)
    nil))

(defn update-task-info
  [^:Map task-map]
  (let [elem (dom/getElement (:id task-map))
        allnodes (map #(-> % .-firstChild)
                      (array-seq (dom/getChildren elem)))]
    (when (:title task-map)
      (set-html! (nth allnodes 1) (:title task-map)))
    (when (:desc task-map)
      (set-html! (nth allnodes 2) (:desc task-map)))))

(defmethod dispatcher "updateTask"
  [^:Map resp ^:Map params]
  (if (= (:status resp) "success")
    (update-task-info params)
    (js/alert "Something went wrong")))

(defmethod dispatcher :default
  [])
