(ns clojure-learning.cricket-data.core
  (:require [net.cgrand.enlive-html :as html]
            [clojure.walk :as walk]))

(def cricket-data-url
  "http://stats.espncricinfo.com/ci/engine/stats/index.html?class=1;opposition=2;team=3;template=results;type=batting")

(defn caption?
  [node]
  (= (:tag node) :caption))

(defn select-nodes
  [selector n]
  (html/select n selector))

(defn table->headers
  [table-node]
  (->> table-node
       (select-nodes [:th :a])
       (map (comp first :content))
       (into [])))

(defn contains-node?
  [selector node]
  (not (-> (select-nodes selector node)
           empty?)))

(defn process-table-cell-link
  [table-cell-content-links]
  (let [content (-> table-cell-content-links first :content)]
    (when (string? (first content))
      content)))

(defn process-table-cell
  [table-cell-node]
  (let [content (table-cell-node :content)]
    (if (-> content first string?)
      content
      (->> content
           (select-nodes [:a])
           process-table-cell-links))))

(defn process-table-row
  [table-row-node]
  (->> table-row-node
       (select-nodes [:td])
       (mapcat process-table-cell)
       (into [])))

(defn table->rows
  [table-node]
  (->> table-node
       (select-nodes [:tr])
       (filter (partial contains-node? [:td]))
       (map process-table-row)))

(defn parse-int
  [s]
  (Integer. (re-find  #"\d+" s )))

(defn contains-page-indicator?
  [node]
  (let [[page _ of _] (-> node :content)]
    (and
     (string? page)
     (boolean (re-find #"Page" page))
     (string? of)
     (boolean (re-find #"of" of)))))

(defn extract-number-of-pages
  [node]
  (let [[_ _ _ pages] (-> node :content)]
    (-> pages :content first)))

(defn get-number-of-pages
  [html-page-nodes]
  (->> (select-nodes [:table :tr.data2 :td] html-page-nodes)
       (filter contains-page-indicator?)
       first
       extract-number-of-pages
       parse-int))

(defn add-page-to-url
  [url n]
  (str url ";page=" n))

(defn fetch-page
  [url]
  (html/html-resource (java.net.URI. url)))

(defn select-data-table-node
  [html-page-nodes]
  (->> html-page-nodes
       (select-nodes [:table])
       (filter contains-caption?)))

(defn cricket-data
  [url]
  (let [first-page-url     (add-page-to-url url 1)
        first-page         (fetch-page first-page-url)
        total-pages        (get-number-of-pages first-page)
        rest-of-page-urls  (map (partial add-page-to-url url)
                                (range 2 (inc total-pages)))
        rest-of-pages      (map fetch-page rest-of-page-urls)
        all-table-rows     (->> (cons first-page rest-of-pages)
                                (mapcat (comp table->rows select-data-table-node))
                                (into []))
        table-headers      (-> first-page select-data-table-node table->headers)]
    {:table-headers table-headers
     :table-rows    all-table-rows}))

(comment
  (def data  (cricket-data cricket-data-url))

  (count (:table-rows data))

)







