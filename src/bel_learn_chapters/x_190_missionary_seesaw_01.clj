(ns bel-learn-chapters.x-190-missionary-seesaw-01
  [:use [seesaw.core]
        [seesaw.font]
        [missionary.core]]
  [:import (com.formdev.flatlaf FlatDarkLaf)])

(defn -main [& args]
  (FlatDarkLaf/setup) ; FIRST install LAF for HDPI
  (let [time-is-running (atom true)
        time-f          #(str "secs: " (long (/ (System/currentTimeMillis) 1000)))
        panel           (flow-panel
                          :items [(label
                                    :id :notified-label
                                    :border 10
                                    :text "initial text a little longer...")
                                  (button :text "stop"
                                          :listen [:action (fn [e] (reset! time-is-running false))])])
        frame           (frame :title "seesaw" :content panel :on-close :dispose)

        time-task       (sp ; sequencial process
                          (config! (select frame [:#notified-label])
                                   :text (time-f)))]

    (invoke-later (-> frame pack! show!))

    (future (while @time-is-running
              (invoke-later (? time-task))
              (Thread/sleep 1000)))))

(-main)

