(def task-options
  {:project 'coconutpalm/boot-http
   :version "0.9.1"
   :project-name "coconutpalm/boot-http"
   :project-openness :open-source
  
   :description "A simple HTTP `serve` task for use with [the boot build tool][boot] that can serve resources, directories or a typical ring handler."
  
   :scm-url "https://github.com/fuse-code/boot-http"
  
   :test-sources "test"
   :test-resources "test-extra/resources"})


(set-env!
 :dev-dependencies '[[peridot "0.4.3"]]
 
 :dependencies     '[[org.clojure/clojure    "1.9.0"]
                     [coconutpalm/boot-boot  "LATEST" :scope "test"]])

(require
 '[clj-boot.core :refer :all]
 '[adzerk.bootlaces :refer :all] ;; tasks: build-jar push-snapshot push-release
 '[adzerk.boot-test :refer :all]
 '[pandeiro.boot-http :refer :all])

(deftask test-boot-http []
  (merge-env!
   :dependencies (concat (get-env :dev-dependencies) serve-deps)
   :resource-paths #{"test-extra/resources"})
  (test :namespaces #{'pandeiro.boot-http-tests}))


(set-task-options! task-options)