(ns todo-frontend.eventmanager
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.history.EventType :as EventType]
            [todo-frontend.apicalls :as apis]
            [todo-frontend.handler :as hand]
            [cljs.core.async :refer [<!]]))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn set-add-listener
  [name]
  (println "addlistener" name)
  (let [addbutton (dom/getElement "addtask")]
    (events/listen addbutton "click"
                   (fn [event]
                     (let [inp_boxes (dom/getElementsByTagName "input")
                           title (.-value (aget inp_boxes 0))
                           desc (.-value (aget inp_boxes 1))]
                       (set! (.-value (aget inp_boxes 0)) "")
                       (set! (.-value (aget inp_boxes 1)) "")
                       (when (not= title "")
                         (go
                           (let [res (<! (apis/add-task-req
                                           name
                                           {:title title :desc desc}))]
                             ;append to tasks on receiveing
                             (if (= 200 (:status res))
                               (hand/dispatcher (:body res) name))))
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
  (let [updaterows (array-seq (dom/getElementsByClass "row-elem"))]
    (doseq [btn updaterows]
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

;(defn set-register-listener
;  []
;  (let [addbutton (dom/getElement "register")]
;    (println addbutton)
;    (events/listen addbutton "click"
;                   (fn [event]
;                     (println "register fired")
;                     (let [inp_boxes (dom/getElementsByTagName "input")
;                           name (.-value (aget inp_boxes 0))
;                           userid (.-value (aget inp_boxes 1))]
;                       (set! (.-value (aget inp_boxes 0)) "")
;                       (set! (.-value (aget inp_boxes 1)) "")
;                       (when (or (not= name "") (not= userid ""))
;                         (go
;                           (let [res (<! (apis/create-new-user
;                                           {:name name :uid userid}))]
;                             ;append to tasks on receiveing
;                             (if (= 200 (:status res))
;                               (secretary/dispatch! (str "/#" name)))))
;                         )))))
;  )