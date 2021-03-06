(ns coconutpalm.boot-http
  {:boot/export-tasks true}
  (:require
   [boot.pod           :as pod]
   [boot.util          :as util]
   [boot.core          :as core :refer [deftask]]
   [boot.task.built-in :as task]
   [boot.repl          :as repl]))

(def default-port 3000)

;keystore can be file path or resource
(def ssl-default-port 3443)
(def ssl-defaults {:port ssl-default-port :keystore (str (clojure.java.io/resource "boot-http-keystore.jks")) :key-password "p@ssw0rd"})

(def serve-deps
  '[[ring/ring-core "1.6.3"]
    [ring/ring-headers "0.3.0"]
    [ring/ring-devel "1.6.3"]])

(def jetty-dep
  '[ring/ring-jetty-adapter "1.6.3"])

(def httpkit-dep
  '[http-kit "2.2.0"])

(def immutant-dep
  '[org.immutant/web "2.1.10"
    :exclusions [ch.qos.logback/logback-classic]])

(defn nrepl-deps
  []
  (letfn [(tools-nrepl? [spec]
            (= 'org.clojure/tools.nrepl (first spec)))]
    (if-not (some tools-nrepl? @repl/*default-dependencies*)
      (conj @repl/*default-dependencies* '[org.clojure/tools.nrepl "0.2.11"])
      @repl/*default-dependencies*)))

(defn- silence-jetty! []
  (.put (System/getProperties) "org.eclipse.jetty.LEVEL" "WARN"))

(deftask serve
  "Start a web server on localhost, serving resources and optionally a directory.
  Listens on port 3000 by default."

  [d dir           PATH str  "The directory to serve; created if doesn't exist."
   H handler       SYM  sym  "The ring handler to serve."
   i init          SYM  sym  "A function to run prior to starting the server."
   c cleanup       SYM  sym  "A function to run after the server stops."
   r resource-root ROOT str  "The root prefix when serving resources from classpath"
   p port          PORT int  "The port to listen on. (Default: 3000)"
   k httpkit            bool "Use Http-kit server instead of Jetty"
   I immutant           bool "Use Immutant server instead of Jetty"
   s silent             bool "Silent-mode (don't output anything)"
   t ssl                bool "Serve via Jetty/Immutant SSL connector on localhost on default port 3443 using cert from ./boot-http-keystore.jks"
   T ssl-props     SSL  edn  "Override default SSL properties e.g. \"{:port 3443, :keystore \"boot-http-keystore.jks\", :key-password \"p@ssw0rd\"}\""
   R reload             bool "Reload modified namespaces on each request."
   n nrepl         REPL edn  "nREPL server parameters e.g. \"{:port 3001, :bind \"0.0.0.0\"}\""
   N not-found     SYM  sym  "a ring handler for requested resources that aren't in your directory. Useful for pushState."
   S charset       CHAR str  "charset to use when serving static resources and files. (Default: utf-8)"]

  (let [ssl-props   (when (or ssl ssl-props)
                      (if (and ssl-props (not (map? ssl-props)))
                        (throw (IllegalArgumentException.
                                 (str "Expected map for ssl-props got \"" ssl-props "\"")))
                        (merge ssl-defaults (or ssl-props {}))))
        port        (if (seq ssl-props) 
                      (or port (:port ssl-props) ssl-default-port)
                      (or port default-port))
        server-dep  (cond 
                      httpkit httpkit-dep 
                      immutant immutant-dep
                      :else jetty-dep)
        deps        (cond-> serve-deps
                      true               (conj server-dep)
                      (not (nil? nrepl)) (concat (nrepl-deps)))

        ;; Turn the middleware symbols into strings to prevent an attempt to
        ;; resolve the namespaces when the list is processed in the Boot pod.
        ;; Boot's middleware configuration is read outside of the pod to ensure
        ;; that the user-specific Boot configuration is also captured.
        middlewares (conj (map str @@(resolve 'boot.repl/*default-middleware*)) 'list)

        worker      (pod/make-pod (update-in (core/get-env) [:dependencies]
                                             into deps))
        start       (delay
                     (pod/with-eval-in worker
                       (require '[coconutpalm.boot-http.impl :as http]
                                '[coconutpalm.boot-http.util :as u]
                                '[boot.util               :as boot])
                       (when '~init
                         (u/resolve-and-invoke '~init))
                       (def server
                         (http/server
                          {:dir ~dir, :port ~port, :handler '~handler,
                           :ssl-props ~ssl-props,
                           :reload '~reload, :env-dirs ~(vec (:directories pod/env)), :httpkit ~httpkit, :immutant ~immutant,
                           :not-found '~not-found,
                           :resource-root ~resource-root}))
                       (def nrepl-server
                         (when ~nrepl
                           (http/nrepl-server {:nrepl (assoc ~nrepl :middleware (map symbol ~middlewares))})))
                       (when-not ~silent
                         (boot/info "Started %s on %s://localhost:%d\n"
                               (:human-name server)
                               (if ~ssl-props "https" "http")
                               (:local-port server)))))]
    (when (and silent (not httpkit) (not immutant))
      (silence-jetty!))
    (core/cleanup
     (pod/with-eval-in worker
       (when nrepl-server
         (when-not ~silent
           (boot/info "Stopping boot-http nREPL server"))
         (.stop nrepl-server))
       (when server
         (when-not ~silent
           (boot/info "Stopping %s\n" (:human-name server)))
         ((:stop-server server)))
       (when '~cleanup
         (u/resolve-and-invoke '~cleanup))))
    (core/with-pre-wrap fileset
      @start
      (assoc fileset :http-port (pod/with-eval-in worker (:local-port server))))))

