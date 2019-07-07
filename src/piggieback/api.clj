(ns piggieback.api
  "nREPL middleware enabling the transparent use of a ClojureScript REPL with nREPL tooling."
  {:author "Chas Emerick"}
  (:refer-clojure :exclude (load-file))
  (:require
   [nrepl.middleware :as middleware :refer [set-descriptor!]]))

(def ^:private cljs-present?
  (try (require 'cljs.repl)
       (require 'piggieback.core)
       true
       (catch java.io.FileNotFoundException e
         false)))

(defn cljs-repl
  "Starts a ClojureScript REPL over top an nREPL session.  Accepts
   all options usually accepted by e.g. cljs.repl/repl."
  [repl-env & {:as options}]
  (if cljs-present?
    (#'piggieback.core/cljs-repl repl-env options)
    (throw (ex-info "Clojurescript not present" {}))))

(defn wrap-cljs-repl [handler]
  (if cljs-present?
    (#'piggieback.core/cljs-aware-handler handler)
    (fn [msg] (handler msg))))

(set-descriptor! #'wrap-cljs-repl
                 {:requires #{"clone"}
                  ;; piggieback unconditionally hijacks eval and load-file
                  :expects #{"eval" "load-file"}
                  :handles {}})
