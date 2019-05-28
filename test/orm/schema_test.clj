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

(s/describe ::person)

; the specs match the fixtures
(s/explain-data ::person {:name "John"
                          :sex  "male"
                          :age  22})

(s/explain-data ::person {:name "John"
                          :sex  "male"})

; spit out the generated code
(let [generated-file "dev/src/hello_world/orm.cljc"
      generated-forms (orm/reads fixtures/police-records)]
  (io/make-parents generated-file)
  (orm/spit-namespace!
    '(ns hello-world.orm
       (:require [datascript.core :as d]))
    generated-forms
    generated-file))

; open and load the generated ns and then..

(defonce global-database
         (d/create-conn {:name {:db/index true}}))

(d/transact! global-database [{:name "John"
                               :sex  "male"
                               :age  22}])

(d/transact! global-database [{:name "Paul"
                               :sex  "male"
                               :age  26}])

(->> (hello-world.orm/find-persons @global-database)
     (s/valid? (s/coll-of ::person)))

(hello-world.orm/all-matching-persons-by-name @global-database
                                              (let [s "Jo"]
                                                (if (empty? s)
                                                  (constantly true)
                                                  #(re-find (re-pattern (str "(?i)" s)) %))))



