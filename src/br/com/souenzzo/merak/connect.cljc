(ns br.com.souenzzo.merak.connect
  (:require [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.core :as p]
            [edn-query-language.core :as eql]))

(def register
  [(pc/resolver `vs-table
                {::pc/params [::join-key]
                 ::pc/output [::vs-table]}
                (fn [{::pc/keys [indexes] :as env} input]
                  (let [{::keys [join-key display-properties]} (p/params env)
                        syms (-> indexes ::pc/index-oir (get join-key) (get #{}))]
                    {::vs-table {::join-params   (for [sym syms
                                                       :let [{::pc/keys [params]} (pc/resolver-data env sym)]
                                                       param params]
                                                   {::pc/attribute param})
                                 ::join-key      join-key
                                 ::table-headers (for [{:keys [dispatch-key]} (:children (eql/query->ast display-properties))]
                                                   {::pc/attribute dispatch-key})}})))])
