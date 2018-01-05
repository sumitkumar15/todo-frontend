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

(defn set-add-listener
  [name]
  (let [addbutton (dom/getElement "addtask")]
    (events/listen addbutton "click"
                   (fn [event]
                     (let [title (.-value (aget (dom/getElementsByTagName "input") 0))
                           desc (.-value (aget (dom/getElementsByTagName "input") 1))]
                       (when (not= title "")
                         (go
                           (let [res (<! (apis/add-task-req
                                           name
                                           {:title title :desc desc}))]
                             ;append to tasks on receiveing
                             (if (= 200 (:status res))
                               (hand/dispatcher (:body res)))))
                         ))))))
(defn listen-delete
  [element name]
  (events/listen element "click"
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
                               (js/alert "Some Error Occured"))))))
                     (.stopPropagation event)
                     false))))

(defn set-delete-listeners
  [name]
  (let [delbtns (array-seq (dom/getElementsByClass "delete-btn"))]
    (doseq [btn delbtns]
      (listen-delete btn name))))

(defn listen-modify
  [element name]
  (events/listen element "click"
                 (fn [event]
                   (let [allnodes (map #(-> % .-firstChild)
                                       (array-seq (dom/getChildren element)))
                         title (.-innerText (nth allnodes 1))
                         desc (.-innerText (nth allnodes 2))
                         inp_ctrls (array-seq (dom/getElementsByClass "modify-controls"))
                         up_btn (dom/getElement "modifytask")
                         ]
                     (set! (.-value (nth inp_ctrls 0)) title)
                     (set! (.-value (nth inp_ctrls 1)) desc)
                     (set! (.-value up_btn) (-> element (.getAttribute "id"))))
                   (.stopPropagation event))))

(defn set-update-listener
  [name]
  (let [upbutton (dom/getElement "modifytask")]
    (events/listen upbutton "click"
                   (fn [event]
                     (let [inp_ctrls (array-seq (dom/getElementsByClass "modify-controls"))
                           title (.-value (nth inp_ctrls 0))
                           desc (.-value (nth inp_ctrls 1))
                           id (.-value upbutton)
                           req-map {:id id :title title :desc desc}]
                       (when (not= title "")
                         (go
                           (let [res (<! (apis/update-req
                                           name
                                           req-map))]
                             ;append to tasks on receiveing
                             (if (= 200 (:status res))
                               (hand/dispatcher (:body res) req-map))))))))))

(defn set-modify-listener
  [name]
  (let [updatebtns (array-seq (dom/getElementsByClass "row-elem"))]
    (doseq [btn updatebtns]
      (listen-modify btn name))))

(defn listen-completed
  [element name]
  (events/listen element "change"
                 (fn [event]
                   (let [target (.-target event)
                         state (.-value target)]
                     (if (= state "on")
                       (set! (.-value element) "off")
                       (set! (.-value element) "on"))
                     (let [taskid (-> (dom/getParentElement element)
                                      dom/getParentElement
                                      (.getAttribute "id"))
                           ]
                       (if (= "on" (.-value element))
                         (do
                           (go
                             (let [req-map {:id taskid :status "C"}
                                   res (<! (apis/update-status
                                             name
                                             req-map))]
                               (if (= 200 (:status res))
                                 (hand/dispatcher (:body res) req-map)))))
                         (do
                           (go
                             (let [req-map {:id taskid :status "I"}
                                   res (<! (apis/update-status
                                             name
                                             req-map))]
                               (if (= 200 (:status res))
                                 (hand/dispatcher (:body res) req-map)))))
                         ))
                   (.stopPropagation event)))))

(defn set-comp-listeners
  [name]
  (let [comp_btns (array-seq (dom/getElementsByClass "chk-box"))]
    (doseq [tick_btn comp_btns]
      (listen-completed tick_btn name))))

(add-watch tasks-state :taskstate
           (fn [_key _atom oldstate newstate]
             (let [content @newstate
                   element (dom/getElement "tasks")]
               (set-html! element content))))

;; Routes definition start
(defroute "/" []
          ;open register page
          (println "on route /")
          ;(main)
          )
(defroute "/:name" [name]
          (set-add-listener name)
          (go
            (let [res (<! (hand/load-tasks name))]
              (reset! tasks-state (atom res))
              (set-delete-listeners name)
              (set-modify-listener name)                    ;listener on row elem
              (set-update-listener name)                    ;listener on update btn
              (set-comp-listeners name))))

;; Routes definition ended
(let [h (History.)]
  (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
