(ns todo-frontend.core
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :refer-macros [defroute]]
            [todo-frontend.apicalls :as apis]
            [todo-frontend.templates :as tpl])
  (:import goog.History))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom "<h1>Initial State</h1>"))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn main
  []
  (let [content @app-state
        element (dom/getElement "maindata")]
    (set-html! element content)))

;(main)

(add-watch app-state :appstate
           (fn [_key _atom oldstate newstate]
             (let [content @newstate
                   element (dom/getElement "maindata")]
               (set-html! element content))))


;(reset! app-state (atom "<h1>Next State</h1>"))
(reset! app-state (atom (tpl/load-test)))
;(dom/appendChild (dom/getElement "maindata") @app-state)
(println @app-state)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
