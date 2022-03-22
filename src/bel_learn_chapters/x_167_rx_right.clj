(ns bel-learn-chapters.x-167-rx-right
  (:require [rx.lang.clojure.core :as rx]
            [tupelo.string :as str]
            [seesaw.core :refer :all])
  (:import [rx Observable]
           [java.util.concurrent TimeUnit]))

; https://reactivex.io/documentation
; source-code of rx.lang.clojure in projects/RxClojure

; domain logic

(defn share-price [company-code]
  (Thread/sleep 200)
  (rand-int 1000))

(defn avg [numbers]
  (float (/ (reduce + numbers)
            (count numbers))))


; gui

(defn create-frame []
  (native!)
  (let [main-frame       (frame :title "Stock price monitor"
                                :width 200 :height 100
                                :on-close :dispose)
        price-label      (label :text "Price: -" :id :price-label)
        moving-avg-label (label :text "AVG: -" :id :moving-avg-label)]

    (config! main-frame
             :content (border-panel
                        :north price-label
                        :center moving-avg-label
                        :border 10))))

; reactive connections


(defn make-price-obs [company-code]
  (rx/return (share-price company-code)))


(defn -main [& args]
 (let [f (create-frame)]
  (show! f)
  (let [price-obs (-> (rx/flatmap (fn [_] (make-price-obs "XYZ"))
                                  (Observable/interval 100 TimeUnit/MILLISECONDS))
                      (.publish)) ; wont start emmitting before connected
        sliding-buffer-obs (.buffer price-obs 50 1)]
    (rx/subscribe price-obs
                  (fn [price]
                    (text! (select f [:#price-label])
                           (str "Price: " price))))
    (rx/subscribe sliding-buffer-obs
                 (fn [buffer]
                   (text! (select f [:#moving-avg-label])
                          (str "AVG: " (avg buffer)))))
    (.connect price-obs))))

(comment (-main))