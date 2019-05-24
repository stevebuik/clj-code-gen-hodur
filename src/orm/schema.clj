(ns orm.schema
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.template :refer [apply-template do-template]]
    [com.rpl.specter :refer [select select-first transform setval
                             ALL LAST FIRST BEGINNING END MAP-VALS NONE INDEXED-VALS
                             collect-one must filterer walker submap]]
    [cljfmt.core :refer [reformat-string]]
    [datascript.query-v3 :as q]
    [datascript.core :as d]))

(defn orm-schema
  [conn]
  (let [selector '[* {:field/_parent
                      [* {:field/type [*]}]}]
        eids (-> (q/q '[:find ?e
                        :where
                        [?e :orm/tag true]                  ; only entities tagged for ORM code generation
                        [?e :type/nature :user]
                        (not [?e :type/interface true])
                        (not [?e :type/union true])]
                      @conn)
                 vec
                 flatten)]
    (->> eids
         (d/pull-many @conn selector)
         (sort-by :type/name))))

(defn pull-symbol
  "return the top level symbol for a datalog pull expression for an entity"
  [entity]
  (symbol (str (name (:type/camelCaseName entity)) "-pull")))

(defn query-symbol
  "return the top level symbol for a datalog query for an entity"
  [entity]
  (symbol (str (name (:type/camelCaseName entity)) "-query")))

(defn fields
  [entity]
  (->> entity
       (select [:field/_parent ALL :field/camelCaseName])
       sort))

(defn pull-expressions
  "return the def form for a datalog pull expression for all entities"
  [schema]
  (->> schema
       (sort-by :type/camelCaseName)
       (map (fn [entity]
              (list 'def
                    (pull-symbol entity)
                    (vec (fields entity)))))))

(defn entity-query
  [entity]
  (let [query-without-conditions (apply-template '[pull-expr]
                                                 '[:find (pull ?e pull-expr)
                                                   :where]
                                                 [(vec (fields entity))])
        conditions (->> (fields entity)
                        (map (fn [field]
                               ['?e field (symbol (str "?" (name field)))])))]
    (into query-without-conditions conditions)))

(defn queries
  [schema]
  (->> schema
       (sort-by :type/camelCaseName)
       (map (fn [entity]
              (apply-template '[q-sym query]
                              '(def q-sym 'query)
                              [(query-symbol entity)
                               (entity-query entity-query)])))))

(defn find-all-fns
  [schema]
  (->> schema
       (sort-by :type/camelCaseName)
       (map (fn [entity]
              (apply-template '[fn-name-sym query-sym]
                              '(defn fn-name-sym
                                 [db]
                                 (->> db
                                      (d/q query-sym)
                                      (map first)))
                              [(symbol (str "find-" (name (:type/camelCaseName entity)) "s"))
                               (query-symbol entity)])))))

(defn match-query
  [entity]
  (let [query-without-conditions (apply-template '[pull-expr]
                                                 '[:find (pull ?e pull-expr)
                                                   :in $ ?pred
                                                   :where]
                                                 [(into [:db/id] (fields entity))])
        conditions (->> (fields entity)
                        (map (fn [field]
                               ['?e field (symbol (str "?" (name field)))])))]
    (into query-without-conditions conditions)))

(defn match-fns
  [schema]
  (->> schema
       (sort-by :type/camelCaseName)
       (mapcat (fn [entity]
                 (->> entity
                      (select [:field/_parent ALL :field/camelCaseName])
                      (map (fn [field]
                             (let [fn-name (symbol (str "all-matching-" (name (:type/camelCaseName entity)) "s-by-" (name field)))
                                   field-in-query (symbol (str "?" (name field)))
                                   query (->> [(list '?pred field-in-query)]
                                              (conj (match-query entity)))]
                               (apply-template '[fn-name-sym query field]
                                               '(defn fn-name-sym
                                                  [db pred]
                                                  (->> pred
                                                       (d/q (quote query) db)
                                                       (map first)
                                                       (sort-by field)))
                                               [fn-name
                                                query
                                                field])))))))))

(defn reads
  [conn]
  (let [schema (orm-schema conn)
        pull-forms (pull-expressions schema)
        query-forms (queries schema)
        find-forms (find-all-fns schema)
        match-forms (match-fns schema)]
    (concat pull-forms
            query-forms
            find-forms
            match-forms)))

(defn- formatted
  [form]
  (reformat-string
    (with-out-str
      (binding [clojure.pprint/*print-right-margin* 120
                clojure.pprint/*print-miser-width* 20]
        (pprint form)))))

(defn spit-namespace!
  [ns-decl content f]
  (spit f (if ns-decl (str (formatted ns-decl) "\n") ""))   ; always write something to restart the file
  (spit f (str ";;;;;; GENERATED : DO NOT EDIT!! ;;;;; \n\n") :append true)
  (doseq [form content]
    (spit f (formatted form) :append true)))
