(def task-options
  {:project 'coconutpalm/boot-server
   :version "0.9.1"
   :project-name "coconutpalm/boot-server"
   :project-openness :open-source
  
   :description "A simple `serve` task for use with [the boot build tool][boot] that can serve resources, directories or a typical ring handler."
  
   :scm-url "https://github.com/coconutpalm/boot-server"
  
   :test-sources "test"
   :test-resources "test-extra/resources"})


(set-env! :resource-paths #{"resources" "site-src"}
          :source-paths   #{"src" "test"}
          
          :dev-dependencies '[[peridot "0.4.3"]]

          :dependencies     '[[org.clojure/clojure    "1.9.0"]
                              [coconutpalm/boot-boot  "LATEST" :scope "test"]])


(require '[clj-boot.core :refer :all]
         '[adzerk.bootlaces :refer :all] ;; tasks: build-jar push-snapshot push-release
         '[adzerk.boot-test :refer :all]
         '[coconutpalm.boot-http :refer :all])


(deftask add-test-dependencies []
  (merge-env!
   :dependencies (concat (get-env :dev-dependencies) serve-deps)
   :resource-paths #{"test-extra/resources"}))


(set-task-options! task-options)