(ns fixtures
  (:require [hodur-engine.core :as hodur]))

(def police-records
  (hodur/init-schema

    '[^{:datomic/tag-recursive  true
        :graphviz/tag-recursive true
        :spec/tag-recursive     true
        :orm/tag-recursive      true
        :graphviz/color         "lightsalmon"}
      Person
      [^String name
       ; could use an enum for sex but that's not how the names.clj data is stored
       ; also, enums are yet implemented in the hodur spec plugin
       ^String sex
       ^Integer age]]

    '[^{:datomic/tag-recursive  true
        :graphviz/tag-recursive true
        :graphviz/color         "navajowhite1"}
      Offence
      [^String description]]

    ))
