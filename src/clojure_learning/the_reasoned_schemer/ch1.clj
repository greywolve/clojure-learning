(ns clojure-learning.the-reasoned-schemer.ch1
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]))


;; #s - success -  a goal that succeeds
;; #u - fails   -  an unsuccessful goal

;; If any goal in (run* [q] goal ...) fails the result is ().
(run* [q]
  u#)

;; true is associated with q if (== true q) succeeds.
(run* [q]
  (== true q))

;; Result is (), since u# causes the goals to fail.
(run* [q]
  u#
  (== true q))

;; order doesn't matter
(run* [q]
  (== true q)
  u#)

;; true is associated with q, if all the goals succeed, and (== true q) succeeds, hence
;; the result is (true).
(run* [q]
  s#
  (== true q))

;; corn is associated with q, and the result is (corn), same reason as above.
(run* [q]
  s#
  (== 'corn q))

;; succeeds because true does equal true
(run* [q]
  (let [q true]
    (== q true)))

;; let can also  be written like this, a closure, basically
(run* [q]
  ((fn [x]
     (== x true))
   true))

(run* [q]
  ((fn [x]
     (== x true))
   false))

;; what if we want to add another variable?

;; q is associated with true, and since we have another variable, x is assocated with true too,
;; and our goal as a whole succeeds.

;; a variable is fresh when it was no association yet.

;; hence q also starts out fresh.

(run* [q]
  (fresh [x]
    (== true x)
    (== true q)))

;; the law of fresh, if x is a fresh variable, then (== v x) succeeds and associates x with v
;; and another law, order does not matter, (== v x) is equal to (== x v)

;; q is associated with true, ditto for x.
(run* [q]
  (fresh [x]
    (== x true)
    (== true q)))

;; is the same as above.
(run* [q]
  (fresh [x]
    (== true x)
    (== q true)))

;; q is associated with an unbound fresh variable, and the result is the symbol _0
;; which it is represented by.
(run* [q]
  s#)

;; this returns the same as above.
(run* [q])

;; result is an unbound variable, since the x in (== x true) is the one introduced
;; by the fresh expresion and neither the x from the let, nor the x introduced in the
;; run expression.
(run* [x]
  (let [x false]
    (fresh [x]
      (== x true))))

;; q here, however, does get bound to true, x does not have to be bound, only the variable
;; introduced by run.
(run* [q]
  (let [x false]
    (fresh [x]
      (== q true))))

;; the value of r is [_0 _1], each different unbound variable gets a different numeric subscript,
;; starting with 0 and incrementing, these are known as reified names of fresh variables.
(run* [r]
  (fresh [x y]
    (== [x y] r)))

;; the value of r is [_0 _1 _0], fresh variables are reified in the order which they appear in
;; the vector, and x and y are different variables, since the 2 different x's are introduced.
(run* [r]
  (fresh [x]
    (let [y x]
      (fresh [x]
        (== [y x y] r)))))

;; the value of r is [_0 _0 _1]
(run* [r]
  (fresh [x]
    (let [y x]
      (fresh [x]
        (== [y y x] r)))))

;; in order for this to succeed both goals must succeed, after (== false q) succeeds, q is
;; bound to false, but when the second goal is then run, q is no longer fresh, it is associated
;; with false, and hence cannot be unified with true, and thus the result is ().
(run* [q]
  (== false q)
  (== true q))

;; now it will succeed, since q is associated with true, and then true unified with true succeeds.
;; the result is (true).
(run* [q]
  (== true q)
  (== true q))

;; q is true, since q and x are the same.
(run* [q]
  (let [x q]
    (== x true)))

;; x gets whatever association r gets, but since both are fresh r remains fresh.
;; when one variable is associated with another the term is that they co-refer, or
;; share.
(run* [r]
  (fresh [x]
    (== x r)))

;; q gets associated with x, which gets associated with true, therefore q will be associated with true.
(run* [q]
  (fresh [x]
    (== x true)
    (== q x)))

;; and order of the clauses does not matter
(run* [q]
  (fresh [x]
    (== q x)
    (== x true)))

;; are q and x different variables? can we prove that they are different?
(run* [q]
  (fresh [x]
    (== true x)
    (== x q)))

;; first 'proof', result of this is (false)
(run* [q]
  (fresh [x]
    (== (= x q) q)))

;; second 'proof', also (false).
;; x and the original q are the same, fresh introduces yet another q, and then
;; we compare their quality, and associate the answer with the original q again.
(run* [q]
  (let [x q]
    (fresh [q]
      (== (= x q) x))))

;; moral of the story: run and fresh both always introduce new variables, whenever they are
;; called.

;; result is false, since question on the first cond line is always false, hence, the else
;; clause is always run.
(cond
  false true
  :else false)

;; fails!
(cond
  false s#
  :else u#)

;; now for the core.logic version.
;; which can be read as: (OR [goal-1 AND goal-2 ... AND goal-n]).
;; conde can take multiple goals in each line (a vector).
;; and it can take multiple lines, which are ORed.
;; first line below fails, since the goals are ANDed together, and if one goal fails
;; then obviously they all fail. The next line in then evaluated, which also fails,
;; and hence the entire thing fails, and the result is ().
(run* [q]
  (conde
   [u# s#]
   [u#]))

;; first line fails, but the second succeeds, which means everything succeeds, thanks
;; to OR.
(run* [q]
  (conde
   [u# u#]
   [s#]))

;; try the first line, it succeeds, try the second, it fails, but we have a success already,
;; so that's all good - we still succeed.
(run* [q]
  (conde
   [s# s#]
   [u#]))

;; order doesn't actually matter, the beauty of logic programming. :)
;; first line fails, so we try the next, which succeeds.
(run* [q]
  (conde
   [u#]
   [s# s#]))

;; so the run variable can be seen as being 'refreshed' after each line of conde, this is quite
;; different to a regular cond, but is similar to cond-> which will run every clause.
;; this actually returns (olive oil), since x first gets associated with the symbol olive,
;; and then gets 'refreshed'. We evaluate the second line, and this time x is associated with
;; the symbol 'oil. finally we 'refresh' x again, and evaluate the final line, which fails, this
;; is fine, since each of our previous associations of x is remembered, and we can return them.
(run* [x]
  (conde
   [(== 'olive x) s#]
   [(== 'oil x) s#]
   [u#]))

;; the law of conde: to get more values from conde, pretend that each successful conde line has failed,
;; refreshing all variables that got an association from that line.

;; and we don't actually need the s#'s at all
(run* [x]
  (conde
   [(== 'olive x)]
   [(== 'oil x)]
   [u#]))

;; the 'e' in conde stands for every line, since every line can succeed.

;; we can change from run*, which means give me all the results, to specifying the number
;; of results we want, in this case we only get (olive).
(run 1 [x]
  (conde
   [(== 'olive x)]
   [(== 'oil x)]
   [u#]))

;; putting a lone success on a line, results in an unbound variable in the results.
;; the line succeds without x actually getting an association of any kind.
;; gives (olive _0 oil)
(run* [x]
  (conde
   [(== 'olive x)]
   [s#]
   [(== 'oil x)]
   [u#]))

;; now the unbound variable is first in the list.
(run* [x]
  (conde
   [s#]
   [(== 'olive x)]
   [(== 'oil x)]
   [u#]))

;; returns (extra olive), we get at most 2 answers.
(run 2 [x]
  (conde
   [(== 'extra x) s#]
   [(== 'virgin x) u#]
   [(== 'olive x) s#]
   [(== 'oil x) s#]
   [u#]))

;; r is associated with  [split pea], and the result is ([split pea]).
(run* [r]
  (fresh [x y]
    (== 'split x)
    (== 'pea y)
    (== [x y] r)))

;; now we get two sets of results r is associated with both [split pea] and
;; also [navy bean], hence the result is ([split pea] [navy bean]).
(run* [r]
  (fresh [x y]
    (conde
     [(== 'split x)  (== 'pea  y)]
     [(== 'navy  x)  (== 'bean y)]
     [u#])
    (== [x y] r)))

;; we could make some soup, result is ([split pea soup] [navy bean soup]).
(run* [r]
  (fresh [x y]
    (conde
     [(== 'split x)  (== 'pea  y)]
     [(== 'navy  x)  (== 'bean y)]
     [u#])
    (== [x y 'soup] r)))

;; how about a function?

(defn teacupo [x]
  (conde
   [(== 'tea x)]
   [(== 'cup x)]
   [u#]))

;; returns (tea cup), as expected.
(run* [x]
  (teacupo x))

;; remember the refresh idea, this will return 3 results,
;; ([false true] [tea true] [cup true]).
;; teacupo, associates two values with x, tea and cup.
;; x also gets associated with false in the 2nd line, after being refreshed.
;; y on the other hand gets associated with true on each line.
(run* [r]
  (fresh [x y]
    (conde
     [(teacupo x) (== y true)]
     [(== x false) (== y true)]
     [u#])
    (== [x y] r)))

;; leaving y out on the 2nd line gives as a fresh variable in the first result.
(run* [r]
  (fresh [x y]
    (conde
     [(teacupo x) (== y true)]
     [(== x false)]
     [u#])
    (== [x y] r)))

;; returns ([_0 _1] [_0 _1]).
;; looks like both occurances of _0 have come from the same variable,
;; ditto for _1.
(run* [r]
  (fresh [x y z]
    (conde
     [(== x y) (fresh [x] (== x z))]
     [(fresh [x] (== y x)) (== z x)]
     [u#])
    (== [y z] r)))

;; but actually each _0 is a different variable.
;; return ([false _0] [_0 false])
(run* [r]
  (fresh [x y z]
    (conde
     [(== x y) (fresh [x] (== x z))]
     [(fresh [x] (== y x)) (== z x)]
     [u#])
    (== x false)
    (== [y z] r)))

;; the result here is (false), which shows (== true q) and (== false q) are both expressions
;; and their value is a goal. in this case though we only treat the second expressions value
;; as the goal, and ignore the first.
(run* [q]
  (let [a (== true q)
        b (== false q)]
    b))

;; same thought process as above tells us that the values of fresh and conde expressions
;; are goals too, and we can once again select only the b goal, and ignore the rest.
;; results in (false).
(run* [q]
  (let [a (== true q)
        b (fresh [x]
            (== x q)
            (== false x))
        c (conde
           [(== true q)]
           [(== false q)])]
    b))
