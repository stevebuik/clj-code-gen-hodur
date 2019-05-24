## Generating Clojure code using Hodor/Datascript

- code generation is everywhere: macros, onegraph.com, clj aws client lib, cljc.java-time, etc
- blog posts, libs (hodur), serene, graphql-code-generator.com
- lisp has an advantage over other target langs

### To the code

- when to use (list ...) vs apply-template
- namespaced keys are important for specs and datomic, makes generation much easier when clashes always avoided
This is not seen in this repo!

### What did I learn:

- could be a macro or generated at start-time but I prefer git/diffs
- sorting is important for diffs
- specter is very useful
- name generation should be separated for top level symbols.
    - design generators in phases where phase1 generates a map of names, symbols, keys etc
    - then use this to generate code. hodur already does CSK for you
    - any re-usable names can be util fns
- formatting is tricky. have bounced it off Daniel Compton to allow generated vs IDE formatters to match
- what's not good in hodur
    - real world has deeper needs e.g. sub-types
    - tags don't make N subsets of schemas easy