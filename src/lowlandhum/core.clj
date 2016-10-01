(ns lowlandhum.core
  (:require
    [clojure.string :as string]
    [hiccup.core :as hiccup]))

(def num-re #"[0-9]+\.")

(defn update-last
  [coll f]
  (update coll (dec (count coll)) f))


(string/split
  (string/replace (slurp "lyrics/lyrics.txt")
                  #" ’]" "")
  #"\n")

(string/split
  (string/replace (slurp "lyrics/lyrics.txt")
                  #"’" "'")
  #"\n")


(->>
  (string/split
    (string/replace (slurp "lyrics/lyrics.txt")
                    #"’" "'")
    #"\n")

  ;; trim
  (map string/trim)

  ;; turn into song datastructure
  (reduce
    (fn [memo elem]
      (cond
        ;; line number
        (re-matches num-re elem)
        (conj memo {:number elem})

        ;; title
        (and (not-empty memo)
             (not (some? (:title (last memo)))))
        (update-last memo #(assoc % :title elem))

        ;; lyrics
        (and (not-empty memo)
             (some? (:title (last memo))))
        (update-last memo #(update % :lines (fn [lines] (conj (or lines []) elem))))

        ;:else
        :else memo))
    [])

  ;; get rid of leading whitespace
  (map
    (fn [{:keys [lines] :as song}]
      (assoc
        song
        :lines
        (loop [lines lines]
          (if (not= "" (first lines))
            lines
            (recur (vec (rest lines))))))))

  ;; get rid of trailing whitespace
  (map
    (fn [{:keys [lines] :as song}]
      (assoc
        song
        :lines
        (loop [lines lines]
          (if (not= "" (last lines))
            lines
            (recur (pop lines)))))))

  ;; split stanzas
  (map
    (fn [{:keys [lines] :as song}]
      (assoc song :lines
        (mapv
          #(string/split % #"\n")
          (string/split (string/join "\n" lines) #"\n\n")))))

  ;; render
  (map
    (fn [{:keys [lines number title]}]
      (hiccup/html
        [:article {:class (string/replace number #"\." "")}
         [:h1 number]
         [:h2 title]
         [:div {:class "lyrics"}
          (for [stanza lines]
            [:section
             (for [line stanza]
               [:p line])])]])))

  ;; write it out
  (string/join "")
  (spit "lyrics.html"))


(hiccup/html
  [:div])

(update-last
 [{:number "33."}
  {:number "34."}
  {:number "35."}
  {:number "36."}
  {:number "37."}
  {:number "38."}
  {:number "39."}
  {:number "40."}]
 #(assoc % :foo :bar))

(rest
  [""
   "foo"])