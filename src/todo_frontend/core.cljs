(ns todo-frontend.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :refer-macros [defroute]]
            [todo-frontend.apicalls :as apis]
            [todo-frontend.templates :as tpl]
            [todo-frontend.handler :as hand]
            [cljs.core.async :refer [<!]])
  (:import goog.History))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce tasks-state (atom "<h1>Initial State</h1>"))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn main
  []
  (let [content @tasks-state
        element (dom/getElement "tasks")]
    (set-html! element content)))
(main)

(defn set-add-listener
  [name]
  (let [addbutton (dom/getElement "addtask")]
    (events/listen addbutton "click"
                   (fn [event]
                     (let [title (.-value (aget (dom/getElementsByTagName "input") 0))
                           desc (.-value (aget (dom/getElementsByTagName "input") 1))]
                       (go
                         (let [res (<! (apis/add-task-req
                                         name
                                         {:title title :desc desc}))]
                           ;append to tasks on receiveing
                           (if (= 200 (:status res))
                             (hand/dispatcher (:body res))
                             )))
                       )))))

(defn set-delete-listeners
  [name]
  (let [delbtns (array-seq (dom/getElementsByClass "delete-btn"))]
    (doseq [btn delbtns]
      (events/listen btn "click"
                     (fn [event]
                       (let [target (.-target event)
                             value (str (.-value target))]
                         (go
                           (let [res (<! (apis/delete-req
                                           name
                                           {:id value}))]
                             (if (= 200 (:status res))
                               (let [body (:body res)]
                                 (if (= "success" (:status body))
                                   (dom/removeNode (dom/getElement value))
                                   (js/alert "Some Error Occured"))))))))))
    ))

(add-watch tasks-state :taskstate
           (fn [_key _atom oldstate newstate]
             (let [content @newstate
                   element (dom/getElement "tasks")]
               (set-html! element content))))

;; Routes definition start
(defroute "/:name" [name]
          (set-add-listener name)
          (go
            (let [res (<! (hand/load-tasks name))]
              (reset! tasks-state (atom res))
              (set-delete-listeners name))))

;; Routes definition ended

;(go
;  (let [res (<! (hand/load-tasks name))]
;    (reset! tasks-state (atom res))))

;(defroute "/ot" []
;          (println "inside route2 /ot")
;          (go
;            (let [res (<! ())]
;              (reset! app-state (atom res))
;              @app-state)))

;(defroute "*" []
;          (reset! app-state (atom (tpl/tpl-notfound))))

(let [h (History.)]
  (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
