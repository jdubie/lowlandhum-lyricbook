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

(defn lyrics-content
  []
  (as-> (slurp "lyrics/lyrics.txt") $

    (string/replace $ #"[“”]" "\"")
    (string/replace $ #"[ ]" "")

    (string/replace $ #"’" "'")

    (string/split $ #"\n")

    ;; trim
    (map string/trim $)

    ;; turn into song datastructure
    (reduce
      (fn [memo elem]
        (cond
          ;; line number
          (re-matches num-re elem)
          (conj memo {:number (string/replace elem #"\." "")})

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
      [] $)

    ;; get rid of leading whitespace
    (map
      (fn [{:keys [lines] :as song}]
        (assoc
          song
          :lines
          (loop [lines lines]
            (if (not= "" (first lines))
              lines
              (recur (vec (rest lines)))))))
      $)

    ;; get rid of trailing whitespace
    (map
      (fn [{:keys [lines] :as song}]
        (assoc
          song
          :lines
          (loop [lines lines]
            (if (not= "" (last lines))
              lines
              (recur (pop lines))))))
      $)

    ;; split stanzas
    (map
      (fn [{:keys [lines] :as song}]
        (assoc song :lines
                    (mapv
                      #(string/split % #"\n")
                      (string/split (string/join "\n" lines) #"\n\n"))))
      $)))

(defn lyrics-html
  []
  (->>
    (lyrics-content)

    ;; render
    (map
      (fn [{:keys [lines number title]}]
        (hiccup/html
          [:article {:class (format "a-%s" number)}

           [:div.title-wrapper
             [:h1 number]
             [:h2 title]]
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
    ["<link class='lh' href=\"https://fonts.googleapis.com/css?family=Cormorant+Garamond|Roboto\" rel=\"stylesheet\">"
     "<div><div>" ;; for testing that we strip out squarespace code
     (format "<style class='lh'>%s</style>" (slurp "src/web/reset.css"))
     (as-> (slurp "src/web/style.css") $
           (string/replace $ #"__BACKGROUND_IMAGE_B64__"
                           (.encodeToString (Base64/getEncoder) (slurp-bytes "img/blank_pages_color_light_cropped.jpg")))
           (format "<style class='lh'>%s</style>" $))
     (format "<div class='lh wrapper'>%s<main class='main'></main><div class=\"articles\">%s%s%s%s</div></div>"
             (slurp "src/web/header.html")
             (slurp "src/web/title.html")
             (let [entries (cons {:number "0" :title "Credits"} (lyrics-content))
                   [left right]
                   (->>
                     entries
                    (map #(select-keys % [:number :title]))
                    (map (fn [{:keys [title number]}]
                           (format "<li><div>%s</div><div class=\"toc-title\">%s</div></li>" number title)))
                    (split-at (int (/ (count entries) 2)))
                    (map #(string/join "" %)))]
               (format "<div class=\"a--1 toc spread\"><h1>Contents</h1><div class=\"contents\"><ul>%s</ul><ul>%s</ul></div>%s</div>"
                       left right
                       "<p>Click the box above to enter the number of the song you would like to view.</p>"))
             (slurp "src/web/credits.html")
             (lyrics-html))
     (format "<script class='lh'>%s</script>" (slurp "src/web/app.js"))
     "</div></div>"]))

(defn write-to-file
  []
  (spit "index.html"
        (format "<html><head>%s</head><body>%s</body></html>"
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                (gen-html))))

(defn build
  []
  (println (format "[%d] building..." (System/currentTimeMillis)))
  #_(pbcopy (gen-html))
  (write-to-file))

(comment

  (lyrics-content)
  (lyrics-html)

  (user/go)

  (pbcopy (gen-html))
  (write-to-file)

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