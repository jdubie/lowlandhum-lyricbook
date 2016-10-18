(ns user
 (:require
  [hawk.core :as hawk]
  [lowlandhum.core :refer [build]]))

(def watcher nil)

(defn go
  []
  (alter-var-root
    (var watcher)
    (fn [watcher]
      (when watcher
        (hawk/stop! watcher))
      (hawk/watch!
       [{:paths ["src/web"]
         :handler (fn [ctx _]
                    (load-file "src/lowlandhum/core.clj")
                    (build)
                    ctx)}]))))