(ns todo-frontend.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :refer-macros [defroute]]
            [todo-frontend.apicalls :as apis]
            [todo-frontend.templates :as tpl]
            [todo-frontend.handler :as hand]
            [todo-frontend.eventmanager :as evm]
            [cljs.core.async :refer [<!]])
  (:import goog.History))

(enable-console-print!)

(defonce tasks-state (atom "<h1>Initial State</h1>"))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(add-watch tasks-state :taskstate
           (fn [_key _atom oldstate newstate]
             (let [content @newstate
                   element (dom/getElement "tasks")]
               (set-html! element content))))

;; Routes definition start
(defroute "/" []
          ;open register page
          (println "on route / first")
          ;(let [elem (dom/getElement "register")]
          ;  (println elem))
          ;(evm/set-register-listener)
          (println "on route /")
          )
(defroute "/:name" [name]
          (evm/set-add-listener name)
          (go
            (let [res (<! (hand/load-tasks name))]
              (reset! tasks-state (atom res))
              (evm/set-delete-listeners name)
              (evm/set-modify-listener name)                    ;listener on row elem
              (evm/set-update-listener name)                    ;listener on update btn
              (evm/set-comp-listeners name))))

;; Routes definition ended
(let [h (History.)]
  (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
