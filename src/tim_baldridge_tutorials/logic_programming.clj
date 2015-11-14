(ns tim-baldridge-tutorials.logic-programming
  (:refer-clojure :exclude [==]))

;; Following along with Tim Baldridges logic programming tutorial series

;; Episode 1
;; https://tbaldridge.pivotshare.com/media/logic-programming-episode-1/9120/feature

;; Takes from the muKanren paper: http://webyrd.net/scheme-2013/papers/HemannMuKanren2013.pdf
;; and modified for Clojure. :)

;; often logic variables need to be singletons to work, so we need
;; a way to generate unique ones
(defn lvar
  ([] (lvar ""))
  ([nm] (gensym (str nm "_"))))

(defn lvar? [v]
  (symbol? v))

(comment
  (lvar "foo")
  (lvar? (lvar "foo"))

)

(defn walk [s u]
  (if-let [pr (get s u)]
    (if (lvar? pr)
      (recur s pr)
      pr)
    u))

(defn unify [s u v]
  (let [u (walk s u)
        v (walk s v)]
    (cond
      (and (lvar? u)
           (lvar? v)
           (= u v))
      (lvar? u) (assoc s u v)
      (lvar? v) (assoc s v u)
     :else (and (= u v) s))))

(comment
(unify {} (lvar "s") 42)
(unify {} (lvar "s") (lvar "s"))

  )

;; goals

(defn == [a b]
  (fn [s]
    (if-let [v (unify s a b)]
      [v]
      [])))

(comment
  ((== 1 1) {})

  )

;; logical and basically
;; a and b are functions here, which take s
(defn -conj
  ([a] a)
  ([a b]
   (fn [s]
     (for [aret (a s)
           :when aret
           bret (b aret)
           :when bret]
       bret
       )))
  ([a b & more]
   (-conj a (apply -conj b more))))

(defn -disj [& goals]
  (fn [s]
    (mapcat (fn [goal]
              (goal s)) goals)))

(comment
  ((== 1 1) {})
  (let [a (lvar "a")
        b (lvar "b")]
    ((-conj
      (== a 1)
      (== b a)) {}))
  (let [a (lvar "a")
        b (lvar "b")]
    ((-disj
      (== 2 1)
      (== 2 1)) {}))

  )
