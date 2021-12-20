(defproject functional-programming-itmo-2021 "0.0.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [nrepl/lein-nrepl "0.3.2"]
                 [org.clojure/core.async "1.4.627"]]
  :profiles {
             :lab_1 {
                     :repl-options {
                                    :init-ns functional-programming-itmo-2021.lab-1.main
                                    :package functional-programming-itmo-2021.lab-1
                                    }
                     :main functional-programming-itmo-2021.lab-1.main
                     }
             :lab_2 {
                     :repl-options {
                                    :init-ns functional-programming-itmo-2021.lab-2.main
                                    :package functional-programming-itmo-2021.lab-2
                                    }
                     :main functional-programming-itmo-2021.lab-2.main
                     }
             :lab_3 {
                     :repl-options {
                                    :init-ns functional-programming-itmo-2021.lab-3.main
                                    :package functional-programming-itmo-2021.lab-3
                                    }
                     :main functional-programming-itmo-2021.lab-3.main
                     }
             }
  )
