(ns clojure-learning.the-reasoned-schemer.ch2
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]))

;; value of this is c.
(let [x (fn [a] a)
      y 'c]
  (x y))

;; value associated with r is ([_0 _1]).
;; since x and y were introduced by fresh and remain unbound.
(run* [r]
  (fresh [y x]
    (== [x y] r)))

;; value of this is the same as above, since v and w are also introduced
;; by fresh.
(run* [r]
  (fresh [v w]
    (== (let [x v
              y w]
          [x y])
         r)))

;; grape, obviously. :) in the book they use car, but that's first in clojure.
(first ['grape 'raisin 'pea])

;; a
(first ['a 'c 'o 'r 'n])

;; value associated with q is true, since a is in fact the first of [a c o r n]
;; hence firsto succeeds, and so does (=== true q).
(run* [q]
  (firsto ['a 'c 'o 'r 'n] 'a)
  (== true q))

;; the value associated with r is pear, since x is associated with r, and then x is
;; associated with pear, this in turn associates r with pear.
(run* [r]
  (fresh [x y]
    (firsto [r y] x)
    (== 'pear x)))

;; firsto is defined like this basically, for clojure we need to use
;; the logic variable aware lcons insteod of the regular cons.
(defn firsto* [p a]
  (fresh [d]
    (== (lcons a d) p)))

;; same result as above, r is associated with pear
(run* [r]
  (fresh [x y]
    (firsto* [r y] x)
    (== 'pear x)))

;; need to understand the definition a little more.
;; (== (lcons a d) p) is basically saying what collection
;; d must a be consed onto, in order to form p.
;; if p is [1 2 3], then a will naturally be bound to 1,
;; and b to [2 3], since (cons 1 [2 3]) is [1 2 3].
(run* [r]
  (fresh [a b]
    (== (lcons a b) [1 2 3])
    (== [a b] r)))

;; what's interesting about firsto is the fact that it takes two arguments
;; instead of one, like the regular first does. this allows us to ask interesting
;; questions, like (firsto [r y] 'pear) , we're asking what must r be, if
;; 'pear is the first item in the collection?



