(ns user
  (:require [sc.api]))

(defn undef-all-execution-points!
  "invoke undefsc for all execution points. useful as a repl command with a keybinding."
  []
  (let [ep-ids (->> (:execution-points @sc.impl.db/db)
                    (map last)
                    (map :sc.ep/id)
                    sort)]
    (doseq [ep-id ep-ids]
      (eval `(sc.api/undefsc ~ep-id)))
    (str "undefined " (count ep-ids) " execution points!")))

(comment                                                    ; from the hodur readme

  (require '[hodur-engine.core :as hodur])

  (require '[hodur-datomic-schema.core :as hodur-datomic])

  (require '[hodur-graphviz-schema.core :as hodur-graphviz])

  (require '[hodur-spec-schema.core :as hodur-spec])

  (def meta-db (hodur/init-schema
                 '[^{:datomic/tag-recursive  true
                     :graphviz/tag-recursive true
                     :spec/tag-recursive     true}
                   Person
                   [^String first-name
                    ^String last-name
                    ^String nick-name]]))

  (def datomic-schema (hodur-datomic/schema meta-db))

  (let [graphviz-schema (hodur-graphviz/schema meta-db)]
    (spit "target/person.gv" graphviz-schema))

  (def specs (hodur-spec/schema meta-db)))