(ns br.com.souenzzo.merak.connect-test
  (:require [clojure.test :refer [deftest]]
            [br.com.souenzzo.merak.connect :as merak.connect]
            [com.wsscode.pathom.core :as p]
            [midje.sweet :refer [fact =>]]
            [com.wsscode.pathom.connect :as pc]))

(pc/defresolver accounts [env input]
  {::pc/params [::base]
   ::pc/output [::accounts]}
  {::accounts (for [i (range 3)]
                {::id   i
                 ::name (str "A " i)})})

(deftest simple
  (let [register (concat [accounts
                          (pc/single-attr-resolver ::pc/attribute ::label name)]
                         merak.connect/register)
        parser (p/parser {::p/plugins [(pc/connect-plugin {::pc/register register})]})
        env {::p/reader               [p/map-reader
                                       pc/reader2
                                       pc/open-ident-reader
                                       p/env-placeholder-reader]
             ::p/placeholder-prefixes #{">"}}]
    (fact
      (parser env
              `[{(::merak.connect/vs-table {::merak.connect/join-key           ::accounts
                                            ::merak.connect/display-properties [::id ::name]})
                 [{::merak.connect/join-params [::pc/attribute
                                                ::label]}
                  {::merak.connect/table-headers
                   [::pc/attribute
                    ::label]}
                  ::merak.connect/join-key]}
                {::accounts [::id
                             ::name]}])
      => {::accounts               [{::id   0
                                     ::name "A 0"}
                                    {::id   1
                                     ::name "A 1"}
                                    {::id   2
                                     ::name "A 2"}]
          ::merak.connect/vs-table {::merak.connect/join-params   [{::pc/attribute ::base
                                                                    ::label        "base"}]
                                    ::merak.connect/join-key      ::accounts
                                    ::merak.connect/table-headers [{::pc/attribute ::id
                                                                    ::label        "id"}
                                                                   {::pc/attribute ::name
                                                                    ::label        "name"}]}})))
