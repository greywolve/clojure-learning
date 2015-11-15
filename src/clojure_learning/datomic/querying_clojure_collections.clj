(ns clojure-learning.datomic.querying-clojure-collections
  (:require [datomic.api :as d :refer [db q]]))

;; examples based on  https://gist.github.com/stuarthalloway/2645453

;; just a scalar binds ?answer to the scalar
;; result: #{[42]}
(q '[:find ?answer
     :in ?answer]
   42)

;; result: #{[:hello]}
(q '[:find ?answer
     :in ?answer]
   :hello)

;; binding multiple scalars
;; result: #{["Bruce" 30]}
(q '[:find ?name ?age
     :in ?name ?age]
   "Bruce" 30)

;; how about a relation?
;; result: #{["Bruce"] ["Alice"] ["Peter"]}
(q '[:find ?first
     :in [[?first ?last]]]
   [["Bruce" "Wayne"]
    ["Peter" "Parker"]
    ["Alice" "Parker"]])

;; adding a where clause requires an actual database source, these begin
;; with '$'. here we are essentially mocking out a database source as a list
;; of tuples. looks like we aren't forced to use 4 tuples [E A V T].
;; result: #{["Wayne"]}
(q '[:find ?last
     :in $a
     :where
     [$a "Bruce" ?last]]
   [["Bruce" "Wayne"]
    ["Peter" "Parker"]
    ["Alice" "Parker"]])

;; it would be annoying to have to include the database source each time for a single
;; database source, we can omit $a, and the query will use it's first argument as the
;; source.
;; same result as above.
(q '[:find ?last
     :where
     ["Bruce" ?last]]
   [["Bruce" "Wayne"]
    ["Peter" "Parker"]
    ["Alice" "Parker"]])

;; how about finding all first names when the last name is 'Parker' ?
;; result: #{["Alice"] ["Peter"]}.
(q '[:find ?first
     :where
     [?first "Parker"]]
   [["Bruce" "Wayne"]
    ["Peter" "Parker"]
    ["Alice" "Parker"]])

;; a simple in memory join of two tuples.
;; result: #{["John" 71]}
(q '[:find ?first ?id
     :in [?last ?first ?email] [?email ?id]]
   ["Wayne" "Bruce" "bwayne@thebatcave.com"]
   ["bwayne@thebatcave.com" 71])

;; or how about two relation bindings?
;; result: #{["Peter" 55] ["Bruce" 71]}
(q '[:find ?first ?id
     :in [[?last ?first ?email]] [[?email ?id]]]
   [["Parker" "Peter" "pparker@thelab.com"]
    ["Wayne" "Bruce" "bwayne@thebatcave.com"]]
   [["bwayne@thebatcave.com" 71]
    ["pparker@thelab.com" 55]])
