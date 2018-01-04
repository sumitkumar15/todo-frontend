(ns todo-frontend.apicalls
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn get-all-tasks
  []
  (go (let [response (<! (http/get "http://localhost:3000/tasks"
                                   {:with-credentials? false}))]
        (if (= 200 (:status response))
          (let [body (:body response)]
            (if (= "success" (:status body))
              (:data body)
              nil))
          nil))))

(defn get-user-tasks
  [username]
  (go (let [response (<! (http/get (str "http://localhost:3000/tasks/" username)
                                   {:with-credentials? false}))]
        (if (= 200 (:status response))
          (let [body (:body response)]
            (if (= "success" (:status body))
              (:data body)
              nil))
          nil))))

;{
; "action":"newTask"
;         "status": success/failed
; "data":{
;         all task params
;             "title" : "my new task"
;             "desc" : "useless as ever"
;         }
; }
(defn add-task-req
  [user params]
  (go (let [req-map {:action "newTask"
                     :data {
                            :title (:title params)
                            :desc (:desc params)
                            }
                     }
            response (<! (http/post (str "http://localhost:3000/tasks/" user)
                                   {:with-credentials? false
                                    :json-params req-map}))]
        response)))
;{
; "action":"deleteTask"
; "taskid":"98721jkfnl"
; }
(defn delete-req
  [user params]
  (go (let [req-map {:action "deleteTask"
                     :taskid (:id params)}
            response (<! (http/post (str "http://localhost:3000/tasks/" user)
                                    {:with-credentials? false
                                     :json-params req-map}))]
        response)))
;update task json
;{
; "action":"updateTask"
; "taskid": id
; "data":{
;         "title" : "my new task"
;         "desc" : "useless as ever"
;         }
; }
(defn update-req
  [user params]
  (go
    (let [req-map {:action "updateTask"
                   :taskid (:id params)
                   :data {:title (:title params)
                          :desc (:desc params)}}
          response (<! (http/post (str "http://localhost:3000/tasks/" user)
                                  {:with-credentials? false
                                   :json-params req-map}))]
      response)))

(defn update-status
  [user params]
  (go
    (let [req-map {:action "updateTask"
                   :taskid (:id params)
                   :data {:status (:status params)}}
          response (<! (http/post (str "http://localhost:3000/tasks/" user)
                                  {:with-credentials? false
                                   :json-params req-map}))]
      response)))
