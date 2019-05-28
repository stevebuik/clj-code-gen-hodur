## Generating Clojure code using Hodur/Datascript

- code generation is everywhere:
    - macros
    - onegraph.com
    - clj aws client lib
    - cljc.java-time
    - serene
    - graphql-code-generator.com
    - Hodur
- [Vals Blog Post](https://vvvvalvalval.github.io/posts/2018-07-23-datascript-as-a-lingua-franca-for-domain-modeling.html) got me started
- lisp has an advantage over other target langs
    - macro tooling is already built to do this
    - most generated artifacts are EDN or cljc (less syntax variation)

### To the code

- when to use (list ...) vs apply-template
- namespaced keys are important for specs and datomic, makes generation much easier when clashes always avoided
This is not seen in this repo!

### Learnings:

- the more layers of the system can be described as data,
the more they can be generated, both client and server side
- start with manually writing code and then look for patterns/opportunities for generation
- don't generate everything, just heavily repetitive boilerplate
- much less bugs FTW!
- less testing required for generated code
- could be loaded:
    - at repl generating clj files for git / diffs (my pref)
    - at system startup time, generation can occur and be loaded
    - by macros which emit the forms generated
- sorting is important if using diffs
- specter is great in generator fns (and in general)
- name generation should be separate for all fns except lowest
    - design generators in phases where phase1 generates a map of names, symbols, keys etc
    - then use this to generate code
    - hodur helps by doing CSK for you
    - any re-usable names can be util fns
- formatting is tricky. would benefit from Daniel Comptons idea to make a new formatter
- provides confidence: easier to migrate generated code to a new API/platform
- allows choice between pre-compile vs runtime data e.g. graphql introspection vs CLJS source
- could provide startup time benefits due to AOT
- could turn any persistence-as-a-service into app-as-a-service

### Conclusion

Predictions: code generation will:
- increase in popularity,
- improve in ease of use and capability
- benefit from AI etc
- be a rare skill now, expected skill in future