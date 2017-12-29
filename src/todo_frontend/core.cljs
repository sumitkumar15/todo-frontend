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

(defonce app-state (atom "<h1>Initial State</h1>"))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn main
  []
  (let [content @app-state
        element (dom/getElement "tasks")]
    (set-html! element content)))
(main)

(add-watch app-state :appstate
           (fn [_key _atom oldstate newstate]
             (let [content @newstate
                   element (dom/getElement "tasks")]
               (set-html! element content))))

(defroute "/:name" [name]
          (println name)
          (go
            (let [res (<! (hand/load-tasks name))]
              (reset! app-state (atom res)))))

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
