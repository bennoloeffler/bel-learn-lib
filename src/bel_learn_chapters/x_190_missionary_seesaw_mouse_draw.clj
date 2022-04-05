(ns bel-learn-chapters.x-190-missionary-seesaw-mouse-draw
  [:use [seesaw.core]
        [seesaw.font]
        [missionary.core]]
  [:import (com.formdev.flatlaf FlatDarkLaf)])

;; TODO: create small example with modules (gui, then more complex to use as tutorial for missionary
;; (191 - module mount: receive random data from server)
;; (massively reduce redraws by notifying only changes)

(defn display [title create-content-fn & args]
  (let [main-frame (frame :title title :on-close :dispose)]
    (invoke-later
      (config! main-frame :content (if (seq args) (apply create-content-fn args) (create-content-fn)))
      (pack! main-frame)
      (show! main-frame))
    main-frame))



(defn -main [& args]
  (FlatDarkLaf/setup) ; FIRST install LAF for HDPI
  (let [time-is-running (atom true)
        time-f          #(str "secs: " (long (/ (System/currentTimeMillis) 1000)))
        create-panel-f  #(flow-panel
                           :items [(label
                                     :id :notified-label
                                     :border 10
                                     :text "initial text a little longer...")
                                   (button :text "stop"
                                           :listen [:action (fn [e] (reset! time-is-running false))])])
        frame           (display "seesaw-first" create-panel-f)
        the-label       (select frame [:#notified-label])
        ;_               (println "label:" the-label)
        ;_               (println "frame:" frame)
        time-task       (sp ; sequencial process
                          (config!
                            the-label
                            :text (time-f)))
        zzz-task        (sleep 1000)]


    (future (while @time-is-running
              (invoke-later (? time-task))
              (Thread/sleep 1000)))
    #_(invoke-later ; no sleeping in event dispatch thread!
        (? (sp
             (? zzz-task)
             (? time-task)
             (? zzz-task)
             (? time-task))))))


#_(? (sp
       (println "bel")))

(comment
 (-main))

