(ns orm.schema-test
  (:require [clojure.pprint :refer [pprint]]
            [clojure.test :refer [deftest is testing]]
            [sc.api]
            [fixtures :as fixtures]
            [orm.schema :as orm]
            [hodur-graphviz-schema.core :as hodur-graphviz]
            [hodur-spec-schema.core :as hodur-spec]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [datascript.core :as d]))

; spit out a diagram to make the schema easier to understand
(let [graphviz-schema (hodur-graphviz/schema fixtures/police-records)]
  (spit "target/clj-syd.gv" graphviz-schema))

; generate specs and load them. since specs are global, do this as part of loading the namespace

(eval (hodur-spec/schema fixtures/police-records))

(deftest specs
  (s/describe ::person)
  (is (nil? (s/explain-data ::person {:name "John"
                                      :sex  "male"
                                      :age  22}))))

(deftest read-namespace-generation
  (let [generated-file "dev/src/hello_world/orm.clj"]
    (io/make-parents generated-file)
    (orm/spit-namespace!
      '(ns hello-world.orm
         (:require [datascript.core :as d]))
      (orm/reads fixtures/police-records)
      generated-file)))

; after the test runs, load the generated ns and then try the exprs below..
(comment

  (defonce global-database
           (d/create-conn {:name {:db/index true}}))

  (d/transact! global-database [{:name "John"
                                 :sex  "male"
                                 :age  22}])

  (->> (hello-world.orm/find-persons @global-database)
       (s/valid? (s/coll-of ::person)))

  )

