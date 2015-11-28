(ns clojure-learning.datomic.querying-clojure-collections
  (:require [datomic.api :as d :refer [db q]]))

;; Examples based on  https://gist.github.com/stuarthalloway/2645453.

;; Just a scalar binds ?answer to the scalar.
;; result: #{[42]}
(q '[:find ?answer
     :in ?answer]
   42)

;; result: #{[:hello]}
(q '[:find ?answer
     :in ?answer]
   :hello)

;; Binding multiple scalars.
;; result: #{["Bruce" 30]}
(q '[:find ?name ?age
     :in ?name ?age]
   "Bruce" 30)

;; How about a relation?
;; result: #{["Bruce"] ["Alice"] ["Peter"]}
(q '[:find ?first
     :in [[?first ?last]]]
   [["Bruce" "Wayne"]
    ["Peter" "Parker"]
    ["Alice" "Parker"]])

;; Adding a where clause requires an actual database source, these begin
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

;; It would be annoying to have to include the database source each time for a single
;; database source, we can omit $a, and the query will use it's first argument as the
;; source.
;; same result as above.
(q '[:find ?last
     :where
     ["Bruce" ?last]]
   [["Bruce" "Wayne"]
    ["Peter" "Parker"]
    ["Alice" "Parker"]])

;; How about finding all first names when the last name is 'Parker' ?
;; result: #{["Alice"] ["Peter"]}.
(q '[:find ?first
     :where
     [?first "Parker"]]
   [["Bruce" "Wayne"]
    ["Peter" "Parker"]
    ["Alice" "Parker"]])

;; A simple in memory join of two tuples.
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

;; The same result as above, but using database expressions instead of relational bindings (apparently faster).
;; Quite interesting that you can query over tuples of things, as if they were a database. Gives us
;; some clues as to datomic's internals.
(q '[:find ?first ?id
     :in $a $b
     :where
     [$a ?last ?first ?email]
     [$b ?email ?id]]
   [["Parker" "Peter" "pparker@thelab.com"]
    ["Wayne" "Bruce" "bwayne@thebatcave.com"]]
   [["bwayne@thebatcave.com" 71]
    ["pparker@thelab.com" 55]])

;; Let's take this a little further, and test our not clauses, and not joins, to explore how they work, and where
;; they'd be useful.

;; If one of our tuples has less elements than it should, then you'll get an error.
;; Notice Oliver Queen's entry uses his full name as a single sting, interestingly, this
;; giwes an IndexOutOfBounds exception. Moral of the story make sure your tuple size matches
;; your bound variables.
(q '[:find ?first ?id
     :in $a $b
     :where
     [$a ?last ?first ?email]
     [$b ?email ?id]]
   [["Parker" "Peter" "pparker@thelab.com"]
    ["Wayne" "Bruce" "bwayne@thebatcave.com"]
    ["Tony" "Stark" "tony@starkindustries.com"]
    ["Oliver Queen" "oliver@queenenterprises.com"]]
   [["bwayne@thebatcave.com" 71]
    ["pparker@thelab.com" 55]])

;; More variables than required doesn't cause any errors.
(q '[:find ?first ?id
     :in $a $b
     :where
     [$a ?last ?first ?email]
     [$b ?email ?id]]
   [["Parker" "Peter" "pparker@thelab.com"]
    ["Wayne" "Bruce" "bwayne@thebatcave.com"]
    ["Tony" "Stark" "tony@starkindustries.com"]
    ["Oliver" "Queen" "oliver@queenenterprises.com" "Extra"]]
   [["bwayne@thebatcave.com" 71]
    ["pparker@thelab.com" 55]])

;; Find all people, that don't have an id number.
(q '[:find ?last ?first
      :in $a $b
      :where
      [$a ?last ?first ?email]
     (not [$b ?email _])]
    [["Parker" "Peter" "pparker@thelab.com"]
     ["Wayne" "Bruce" "bwayne@thebatcave.com"]
     ["Tony" "Stark" "tony@starkindustries.com"]
     ["Oliver" "Queen" "oliver@queenenterprises.com"]]
    [["bwayne@thebatcave.com" 71]
     ["pparker@thelab.com" 55]])

;; Find all people, who are not Spiderman. Notice that we are forced to use
;; not-join here. At first I thought you could just use two not clauses separately,
;; but then you have the issue that ?id doesn't get bound.
(q '[:find ?last ?first
     :in $a $b $c
     :where
     [$a ?last ?first ?email]
     (not-join [?email]
               [$b ?email ?id]
               [$c ?id "Spiderman"])]
   [["Parker" "Peter" "pparker@thelab.com"]
    ["Wayne" "Bruce" "bwayne@thebatcave.com"]
    ["Tony" "Stark" "tony@starkindustries.com"]
    ["Oliver" "Queen" "oliver@queenenterprises.com"]]
   [["bwayne@thebatcave.com" 71]
    ["pparker@thelab.com" 55]]
   [[71 "Batman"]
    [55 "Spiderman"]])

;; Attempting to use two not clauses instead fails with:
;; [?id] not bound in not clause: (not-join [?email ?id] [$b ?email ?id])
;; Looks like (not ?a ?b) is coverted to a (not-join [?a ?b] ...) under the hood.
;; Also it makes sense that ?id might not be bound, making the final clause
;; impossible to check.
(q '[:find ?last ?first
     :in $a $b $c
     :where
     [$a ?last ?first ?email]
     (not [$b ?email ?id])
     (not [$c ?id "Spiderman"])]
   [["Parker" "Peter" "pparker@thelab.com"]
    ["Wayne" "Bruce" "bwayne@thebatcave.com"]
    ["Tony" "Stark" "tony@starkindustries.com"]
    ["Oliver" "Queen" "oliver@queenenterprises.com"]]
   [["bwayne@thebatcave.com" 71]
    ["pparker@thelab.com" 55]]
   [[71 "Batman"]
    [55 "Spiderman"]])

