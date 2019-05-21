(ns user)

(require '[hodur-engine.core :as hodur])

(require '[hodur-datomic-schema.core :as hodur-datomic])

(def meta-db (hodur/init-schema
               '[^{:datomic/tag-recursive  true
                   :graphviz/tag-recursive true}
                 Person
                 [^String first-name
                  ^String last-name
                  ^String nick-name]]))

(def datomic-schema (hodur-datomic/schema meta-db))

(require '[hodur-graphviz-schema.core :as hodur-graphviz])

(let [graphviz-schema (hodur-graphviz/schema meta-db)]
  (spit "target/person.gv" graphviz-schema))