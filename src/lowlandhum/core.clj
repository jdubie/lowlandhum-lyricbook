(ns lowlandhum.core
  (:require
    [clojure.string :as string]
    [hiccup.core :as hiccup]
    [clojure.java.io :as io])
  (:import
    [java.util Base64]
    [java.io ByteArrayOutputStream]))

(def num-re #"[0-9]+\.")

(defn update-last
  [coll f]
  (update coll (dec (count coll)) f))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (ByteArrayOutputStream.)]
    (clojure.java.io/copy (io/input-stream x) out)
    (.toByteArray out)))

(defn pbcopy
  "Copies pretty-printed string to clipboard"
  [s]
  (let [p (.. (Runtime/getRuntime) (exec "pbcopy"))
        o (clojure.java.io/writer (.getOutputStream p))]
    (binding [*out* o] (spit o s))
    (.close o)
    (.waitFor p)))

(defn lyrics-html
  []
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
          [:article {:class (format "a-%s" (string/replace number #"\." ""))}
           [:h1 number]
           [:h2 title]
           [:div
            (for [stanza lines]
              [:section
               (for [line stanza]
                 [:p line])])]])))

    ;; write it out
    (string/join "")))

(defn gen-html
  []
  (string/join
    ["<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
     "<link href=\"https://fonts.googleapis.com/css?family=Cormorant+Garamond|Roboto\" rel=\"stylesheet\">"
     (format "<style>%s</style>" (slurp "src/web/reset.css"))
     (as-> (slurp "src/web/style.css") $
           (string/replace $ #"__BACKGROUND_IMAGE_B64__"
                           (.encodeToString (Base64/getEncoder) (slurp-bytes "img/background.png")))
           (format "<style>%s</style>" $))
     (format "<div class='wrapper'>%s<main class='main'></main></div><div class=\"articles\">%s</div>"
             (slurp "src/web/header.html") (lyrics-html))
     (format "<script>%s</script>" (slurp "src/web/app.js"))]))

(defn build
  []
  (println (format "[%d] building..." (System/currentTimeMillis)))
  (pbcopy (gen-html))
  (spit "index.html" (gen-html)))

(comment

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
     "foo"]))