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
- could be loaded:
    - at repl generating clj files for git / diffs (my pref)
    - at system startup time, generation can occur and be loaded
    - by macros which emit the forms generated
- sorting is important if using diffs
- specter is very useful
- name generation should be separate for all fns except lowest
    - design generators in phases where phase1 generates a map of names, symbols, keys etc
    - then use this to generate code
    - hodur helps by doing CSK for you
    - any re-usable names can be util fns
- formatting is tricky. would benefit from Daniel Comptons idea to make a new formatter

