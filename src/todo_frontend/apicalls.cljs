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
  (println (str "http://localhost:3000/tasks/" username))
  (go (let [response (<! (http/get (str "http://localhost:3000/tasks/" username)
                                   {:with-credentials? false}))]
        (if (= 200 (:status response))
          (let [body (:body response)]
            (if (= "success" (:status body))
              (:data body)
              nil))
          nil))))

(defn make-post-req
  [params]
  (go (let [response (<! (http/post "http://localhost:3000/tasks"
                                   {:with-credentials? false}))]
        (prn (:status response))
        (prn (:body response))))
  )