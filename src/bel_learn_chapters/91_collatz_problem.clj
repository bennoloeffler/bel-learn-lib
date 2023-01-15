(ns bel-learn-chapters.91-collatz-problem)


;;
;; --------------------- experiment with COLLATZ Problem ------------------------------------
;;

(def counter (atom 0))

(defn reduce-strange
  "reduce by formula of COLLATZ (n+1) = if (even? n) return n/2 else return 3*n+1
   until 1 is reached.
   return the number of steps the data in form of
   {:num 8 :steps 3}
   meaning it took 3 iterations to reduce 8 to 1 => 8 -> 4 -> 2 -> 1"
  [num]
  (when (= 0 (mod num 10000000))
    (swap! counter + 10000000)
    (locking *out*
      (println "num: " num "count: " @counter " <-- " (.getName (Thread/currentThread)))))
  (loop [n num steps 0]
    (if (< n 2) ;don't check for the sensation ;-)
      {:num num :steps steps}
      (recur (if (odd? n)
               (+ (*' 3 n) 1)
               (quot n 2)) (+ steps 1)))
    #_(cond (< n 2) {:num num :steps steps}
            (> n 1000000000000000000000) {:num num :steps :sensation}
            :else (recur (if (odd? n)
                           (+ (* 3 n) 1)
                           (quot n 2)) (inc steps)))))

(defn reduce-strange-range-raw-data
  "a sequence of numbers and their 'iteration steps count' in form of:
  (reduce-strange-range-raw-data 10 13)
  =>
  ({:num 10, :steps 6} {:num 11, :steps 14} {:num 12, :steps 9})"
  [from to]
  (map reduce-strange (range from to)))

(defn get-list
  "based on raw data, this gets a list of the numbers keyed by the number of iteration steps:
  (get-list (reduce-strange-range-raw-data 10 20))
  =>
  [[4 [16]] [6 [10]] [9 [12 13]] [12 [17]] [14 [11]] [17 [14 15]] [20 [18 19]]]"
  [data]
  (->> data
       (group-by :steps)
       (map (fn [e] [(first e) (vec (map :num (second e)))]))
       (sort-by first)
       ;doall
       vec))

(defn get-hist
  "based on get-list, creates a histogram
  (get-hist(get-list (reduce-strange-range-raw-data 10 20)))
  =>
  ([4 1] [6 1] [9 2] [12 1] [14 1] [17 2] [20 2])"
  [data]
  (->> data
       (map (fn [e] [(first e) (count (second e))]))))

(defn reduce-strange-range-fast
  "without all the intermediate data of
   (get-hist(get-list(reduce-strange-range-raw-data 10 20)))
   creates the histogram:
   ([4 1] [7 1] [9 2] [12 1] [14 1] [17 2] [20 2])
   "
  ; TODO: slow version delivers ([4 1] ***[6 1]*** [9 2] [12 1] [14 1] [17 2] [20 2])
  [from to]
  (loop [n to hist {}]
    (let [result (reduce-strange n)]
      (if (> n from)
        (recur (dec n) (update hist (result :steps) (fnil inc 0))) ; non-transient version
        (sort-by key hist)))))

(reduce-strange-range-fast 10 20)

(defn reduce-strange-range-fast-transient
  "like reduce-strange-range-fast, but with transient map"
  [from to]
  (let [start (System/currentTimeMillis)]
    (loop [n to hist (transient {})]
      (let [result (reduce-strange n)]
        (if (> n from)
          ;(recur (dec n) (update hist (result :steps) (fnil inc 0))) ; non-transient version
          (recur (- n 1)
                 (let [k (result :steps) v (hist k)]
                   (assoc! hist k (if (nil? v) 1 (+ v 1)))))
          (let [end   (System/currentTimeMillis)
                msecs (- end start)
                num   (- to from)
                num-per-sec (quot (* 1000 num) msecs)]
            (locking *out*
              (println "did " num "nums in " (/ msecs 1000.0) "s --> " num-per-sec " nums per sec --> " (quot (* 3600 num-per-sec) 1000000) " Mio nums per h"))
            (sort-by key (persistent! hist))))))))


(comment
  (time (count (reduce-strange-range-fast-transient 10 1000000)))
  (do
    (def from-lower-num 0)
    (def to-upper-num 10000000)
    (println "slow - with data")
    (time (count (get-hist (get-list (reduce-strange-range-raw-data from-lower-num to-upper-num)))))
    (time (count (get-hist (get-list (reduce-strange-range-raw-data from-lower-num to-upper-num)))))
    (time (count (get-hist (get-list (reduce-strange-range-raw-data from-lower-num to-upper-num)))))
    (time (count (get-hist (get-list (reduce-strange-range-raw-data from-lower-num to-upper-num)))))
    (println "fast - but NOT transient")
    (time (count (reduce-strange-range-fast from-lower-num to-upper-num)))
    (time (count (reduce-strange-range-fast from-lower-num to-upper-num)))
    (time (count (reduce-strange-range-fast from-lower-num to-upper-num)))
    (time (count (reduce-strange-range-fast from-lower-num to-upper-num)))
    (println "now transient")
    (time (count (reduce-strange-range-fast-transient from-lower-num to-upper-num)))
    (time (count (reduce-strange-range-fast-transient from-lower-num to-upper-num)))
    (time (count (reduce-strange-range-fast-transient from-lower-num to-upper-num)))
    (time (count (reduce-strange-range-fast-transient from-lower-num to-upper-num)))))


(comment
  (time (def hist-data-mios (reduce-strange-range-fast-transient 0 1000000000))) ;10000000 20000000)))
  (require '[vlaaad.reveal :as reveal])
  (add-tap (reveal/ui))
  (tap> hist-data-mios)

  (def sub-data (-> hist-data-mios vec (subvec 800 (count hist-data-mios))))
  (tap> sub-data))



(defn create-segments
  "create about equal sized segments of numbers
   from start to end
   and create number-of-junks junks.
   If the junks are not even, fill up, so that total fits.
   (create-sements 2 13 3)
   =>
   [[2 6] [6 10] [10 13]]
   "
  [start end number-of-junks]
  (let [len      (- end start)
        one-part (quot len number-of-junks)
        rest     (mod len number-of-junks)]
    (loop [next-start start ranges [] r rest]
      (let [r-distribute (if (> r 0) 1 0)
            next-end     (+ next-start one-part r-distribute)]
        (if (< next-end end)
          (recur next-end (conj ranges [next-start next-end]) (dec r))
          (conj ranges [next-start end]))))))


(defn create-ranges
  "see create-segments.
   (create-ranges 5 12 3)
   =>
   [(5 6 7) (8 9) (10 11)]
   "
  [start end number-of-junks]
  (->> (create-segments start end number-of-junks)
       (map #(range (first %) (second %)))))


(defn merge-histograms
  "merge hists by adding values for keys of the form

   [ [ [1 3][2 4] ]
     [ [1 5][3 9] ] ]
   =>
   [ [1 8][2 4][3 9] ]

   supply args either als col of histograms like this:
   (merge-histograms [  [[1 3][2 4]]  [[1 5][3 9]]  ] )
   or as individual params:
   (merge-histograms  [[1 3][2 4]]   [[1 5][3 9]] )"
  ([one-hist & hists] (merge-histograms (conj hists one-hist)))
  ([hists]
   (->> hists
        (map #(into {} %))
        (reduce #(merge-with + %1 %2) {})
        (sort-by key)
        vec)))

(defn scale-histogram [hist]
  "scales the values of the histogram by dividing them all by the max positive value.
   (scale-histogram [[988 7][989 17][990 2][991 9][993 4]])
   =>
   [[988 0.4117647058823529] [989 1.0] [990 0.11764705882352941] [991 0.5294117647058824] [993 0.23529411764705882]]"
  (let [max (apply max (map second hist))]
    (->> hist
         (map (fn [elem]
                [(first elem)
                 (/ (second elem) (double max))]))
         vec)))



(defn reduce-strange-parallel
  [start end]
  ;really parallel with the right size of processors n=8
  (let [procs  (.. Runtime getRuntime availableProcessors)
        ;max     1000000000
        ;part    (quot max procs)
        ranges (create-segments start end (* 600 procs))
        ;_ (cprint ranges)
        result (->> ranges
                    (pmap (fn [segment] (reduce-strange-range-fast-transient (first segment) (second segment)))))]
    ;(map deref))
    ;doall)
    ;_ (cprint result)]
    ;(cprint :finished)
    (merge-histograms result)))

; (long 1e18) ;works
; (long 1e19) ;to big!
(comment
  (def p-result (time (reduce-strange-parallel 0 (->> 10000000 (* (quot 3600 7)) (* 7))))) ; 36 Mrd = 35980000000
  (def p-result (time (reduce-strange-parallel 0 (long 7e9)))) ; 1h
  (def p-result (time (reduce-strange-parallel 0 (long 50e6)))) ; 10s
  (def p-result (time (reduce-strange-parallel (long 50e9) (long (+ 50e9 100e6))))) ; 70 sec
  (def p-result (time (reduce-strange-parallel 100000000000 (+ 100000000000 1000000000)))) ; 70 sec
  ;(apply merge-with + [{1 10 2 20 3 30}{0 5 1 10 2 20}])
  (->> 10000000 (* (quot 3600 7)) (* 7))
  (def scaled-result (scale-histogram p-result))
  (def sub-data (-> p-result (subvec 900 (- (count p-result) 0)))))


#_(comment
    (def interval-len 10000)
    (def start-of-interval 0)
    (time (def all-data (->> (reduce-strange-range-raw-data start-of-interval (+ start-of-interval interval-len))
                             (group-by :steps)
                             (map (fn [e] [(first e) (vec (map :num (second e)))]))
                             (sort-by first)
                             ;doall
                             vec)))
    (time (def hist (->> all-data
                         (map (fn [e] [(first e) (count (second e))])))))
    hist
    all-data


    (count all-data)
    (def sub-data (-> all-data
                      vec
                      ;last
                      (subvec 0 20)))
    sub-data)
#_all-data

#_(cprint all-data)


#_sub-data
#_(def sub-hist (->> sub-data
                     (map (fn [e] [(first e) (count (second e))]))))

#_sub-hist


#_(def result (->> all-data
                   vec
                   (subvec 350 450)
                   (map (fn [e] [(first e) (count (second e))]))))
#_result


(comment
  (time (reduce-strange 1234400000000000000000000000000000000000099999999999999999999000000000)))

#_(def task-group [[{:id      :count-patterns
                     :timeout 10000
                     :default 0
                     :tasks   [{:id :1 :task '(+ (hard-task #"XY") (hard-task #"YX")) :timeout 10000 :default 0}
                               {:id :2 :task '(+ (hard-task #"ABC") (hard-task #"CBA")) :timeout 10000 :default 0}
                               {:id :3 :task '(+ (hard-task #"VVV") (hard-task #"ZZZ")) :timeout 10000 :default 0}]}
                    '(- :2 (+ :1) :3)]
                   [{:id      :numbers
                     :timeout 10000
                     :default 0
                     :tasks   [{:id :r1 :task '(reduce-strange-range-raw-data 1 10000) :timeout 10000 :default 0}
                               {:id :avg :task '(+ (hard-task #"ABC") (hard-task #"CBA")) :timeout 10000 :default 0}
                               {:id :red :task '(+ (hard-task #"VVV") (hard-task #"ZZZ")) :timeout 10000 :default 0}]}
                    '(- :2 (+ :1) :3)]])


(comment
  (def data-10-pow-9 [[0 1] [1 1] [2 1] [3 1] [4 1] [5 2] [6 2] [7 4] [8 4] [9 6] [10 6] [11 8] [12 10] [13 14] [14 18] [15 24] [16 29] [17 36] [18 44] [19 58] [20 72] [21 91] [22 113] [23 143] [24 179] [25 227] [26 287] [27 366] [28 460] [29 578] [30 731] [31 925] [32 1173] [33 1473] [34 1863] [35 2348] [36 2891] [37 3676] [38 4392] [39 5618] [40 7162] [41 8230] [42 10631] [43 11572] [44 15084] [45 19617] [46 20372] [47 26927] [48 31418] [49 35195] [50 47046] [51 43727] [52 59392] [53 80215] [54 71363] [55 98154] [56 83666] [57 114597] [58 159276] [59 127676] [60 180700] [61 208914] [62 196553] [63 281226] [64 204370] [65 298126] [66 431427] [67 303512] [68 447182] [69 297319] [70 446432] [71 664207] [72 429515] [73 650786] [74 604515] [75 616379] [76 942200] [77 564011] [78 878257] [79 1354330] [80 793551] [81 1244912] [82 695206] [83 1110277] [84 1755113] [85 961543] [86 1546886] [87 1289655] [88 1325626] [89 2146849] [90 1106221] [91 1821538] [92 2533574] [93 1506498] [94 2495910] [95 1216196] [96 2048461] [97 3412489] [98 1642771] [99 2780112] [100 1430443] [101 2214900] [102 3767122] [103 1727056] [104 2982790] [105 3884287] [106 2313321] [107 4011600] [108 1759350] [109 3096512] [110 5391318] [111 2343908] [112 4140800] [113 1741364] [114 3121571] [115 5533124] [116 2311299] [117 4154959] [118 4673398] [119 3067379] [120 5531573] [121 2228194] [122 4072433] [123 6529316] [124 2949885] [125 5403763] [126 2105816] [127 3907028] [128 7170837] [129 2783231] [130 5172984] [131 3257357] [132 3679387] [133 6850533] [134 2582537] [135 4865936] [136 7877344] [137 3412060] [138 6436374] [139 2364675] [140 4510303] [141 8518086] [142 3124688] [143 5963477] [144 2141647] [145 4128761] [146 7886427] [147 2832159] [148 5460317] [149 7083034] [150 3746505] [151 7224764] [152 2549296] [153 4956823] [154 8718690] [155 3375980] [156 6564022] [157 2284815] [158 4472544] [159 8692546] [160 3029248] [161 5927074] [162 5199363] [163 4015862] [164 7854165] [165 2710108] [166 5329593] [167 9533665] [168 3598297] [169 7070387] [170 2421135] [171 4777243] [172 9381482] [173 3217476] [174 6344551] [175 2160382] [176 4276507] [177 8427549] [178 2871235] [179 5680469] [180 8273408] [181 3814944] [182 7544617] [183 2554209] [184 5068849] [185 9995875] [186 3393676] [187 6734844] [188 2262571] [189 4507576] [190 8945678] [191 3005958] [192 5988588] [193 6288091] [194 3991782] [195 7951449] [196 2647351] [197 5297768] [198 10011302] [199 3513123] [200 7032197] [201 2318550] [202 4662905] [203 9334282] [204 3075747] [205 6188032] [206 2032663] [207 4080559] [208 8209997] [209 2675230] [210 5411926] [211 9747226] [212 3546132] [213 7176573] [214 2309598] [215 4702421] [216 9518819] [217 3060654] [218 6233970] [219 1978082] [220 4057446] [221 8266353] [222 2621618] [223 5376778] [224 6767746] [225 3473151] [226 7126044] [227 2226594] [228 4602358] [229 9114556] [230 2949207] [231 6097550] [232 1874828] [233 3908266] [234 8080928] [235 2484038] [236 5177705] [237 2375205] [238 3291638] [239 6861619] [240 2073823] [241 4361720] [242 8664818] [243 2748875] [244 5780385] [245 1716561] [246 3643413] [247 7659879] [248 2276384] [249 4829110] [250 1408334] [251 3017635] [252 6402402] [253 1870289] [254 4005088] [255 6149701] [256 2482921] [257 5311709] [258 1524886] [259 3295112] [260 6862648] [261 2026041] [262 4375095] [263 1234955] [264 2693503] [265 5809611] [266 1643159] [267 3579551] [268 1689434] [269 2186243] [270 4759450] [271 1323312] [272 2910042] [273 6161399] [274 1763851] [275 3872155] [276 1059712] [277 2350689] [278 5155450] [279 1414501] [280 3133063] [281 844242] [282 1888354] [283 4175669] [284 1129349] [285 2521609] [286 4788944] [287 1509713] [288 3364761] [289 897737] [290 2018905] [291 4395139] [292 1202440] [293 2700382] [294 710418] [295 1610123] [296 3607783] [297 952993] [298 2155364] [299 1350592] [300 1277782] [301 2884575] [302 752019] [303 1713027] [304 3780320] [305 1009483] [306 2296343] [307 589694] [308 1355262] [309 3078600] [310 793550] [311 1819070] [312 461109] [313 1067099] [314 2440517] [315 621574] [316 1434346] [317 2990971] [318 837678] [319 1929918] [320 484623] [321 1128571] [322 2593694] [323 654727] [324 1519559] [325 377033] [326 882883] [327 2044462] [328 510218] [329 1190978] [330 1271589] [331 689007] [332 1606018] [333 395391] [334 930742] [335 2127144] [336 535766] [337 1256755] [338 305554] [339 724032] [340 1695927] [341 414673] [342 980534] [343 256632] [344 562830] [345 1326134] [346 320356] [347 762114] [348 1737411] [349 435793] [350 1033894] [351 246727] [352 591451] [353 1399084] [354 335914] [355 801647] [356 189625] [357 456663] [358 1087247] [359 257816] [360 620163] [361 962114] [362 350742] [363 841277] [364 196914] [365 477410] [366 1125289] [367 268524] [368 649618] [369 150211] [370 366805] [371 884137] [372 205308] [373 499931] [374 184016] [375 281077] [376 682146] [377 156481] [378 383932] [379 907185] [380 214119] [381 523208] [382 118421] [383 292795] [384 713945] [385 162073] [386 399619] [387 89164] [388 222570] [389 546157] [390 122518] [391 304467] [392 594848] [393 168644] [394 417057] [395 92589] [396 231713] [397 563028] [398 127975] [399 318077] [400 69927] [401 175931] [402 436379] [403 96333] [404 241846] [405 103645] [406 133196] [407 332689] [408 72821] [409 183707] [410 449529] [411 100527] [412 252528] [413 54778] [414 138840] [415 346721] [416 75803] [417 191136] [418 41258] [419 104954] [420 263831] [421 57122] [422 144971] [423 325806] [424 78818] [425 199645] [426 42522] [427 109144] [428 272035] [429 59060] [430 150801] [431 31787] [432 82182] [433 208740] [434 44209] [435 113481] [436 75381] [437 61318] [438 157018] [439 32981] [440 85304] [441 215353] [442 45852] [443 118435] [444 24379] [445 63802] [446 163770] [447 34176] [448 88848] [449 18049] [450 47668] [451 123328] [452 25389] [453 66468] [454 159379] [455 35744] [456 92880] [457 19132] [458 50041] [459 128960] [460 26523] [461 69441] [462 14005] [463 37243] [464 96678] [465 19621] [466 51863] [467 68544] [468 27689] [469 72678] [470 14533] [471 38682] [472 99981] [473 20567] [474 54217] [475 10721] [476 28911] [477 75673] [478 15097] [479 40279] [480 9221] [481 21141] [482 56167] [483 11016] [484 29793] [485 77116] [486 15556] [487 41783] [488 8150] [489 21985] [490 58481] [491 11424] [492 31024] [493 5852] [494 16242] [495 43510] [496 8347] [497 22905] [498 41276] [499 11867] [500 32398] [501 6046] [502 16752] [503 44806] [504 8601] [505 23764] [506 4388] [507 12297] [508 33623] [509 6313] [510 17550] [511 5891] [512 9119] [513 24947] [514 4708] [515 12943] [516 34371] [517 6696] [518 18331] [519 3437] [520 9559] [521 26028] [522 5032] [523 13779] [524 2608] [525 7202] [526 19470] [527 3827] [528 10285] [529 22210] [530 5526] [531 14689] [532 2893] [533 7843] [534 20522] [535 4164] [536 11143] [537 2221] [538 5941] [539 15795] [540 3214] [541 8472] [542 3578] [543 4520] [544 11937] [545 2388] [546 6382] [547 16698] [548 3445] [549 9040] [550 1819] [551 4791] [552 12637] [553 2549] [554 6759] [555 1329] [556 3575] [557 9527] [558 1902] [559 5053] [560 12508] [561 2647] [562 7149] [563 1386] [564 3789] [565 10033] [566 1943] [567 5354] [568 1006] [569 2771] [570 7560] [571 1464] [572 3958] [573 2590] [574 2066] [575 5623] [576 1111] [577 3016] [578 7985] [579 1577] [580 4256] [581 813] [582 2252] [583 5965] [584 1253] [585 3218] [586 598] [587 1674] [588 4531] [589 861] [590 2367] [591 6084] [592 1217] [593 3367] [594 643] [595 1797] [596 4774] [597 907] [598 2546] [599 473] [600 1329] [601 3604] [602 672] [603 1892] [604 2777] [605 989] [606 2675] [607 492] [608 1382] [609 3729] [610 732] [611 2015] [612 362] [613 1038] [614 2826] [615 524] [616 1514] [617 369] [618 761] [619 2127] [620 410] [621 1111] [622 2895] [623 570] [624 1571] [625 301] [626 815] [627 2191] [628 390] [629 1105] [630 198] [631 575] [632 1592] [633 285] [634 831] [635 1457] [636 403] [637 1153] [638 186] [639 574] [640 1622] [641 273] [642 818] [643 122] [644 400] [645 1223] [646 192] [647 595] [648 193] [649 285] [650 851] [651 126] [652 396] [653 1174] [654 190] [655 615] [656 95] [657 310] [658 899] [659 161] [660 457] [661 79] [662 219] [663 680] [664 116] [665 344] [666 758] [667 177] [668 507] [669 99] [670 267] [671 710] [672 147] [673 391] [674 72] [675 201] [676 545] [677 115] [678 309] [679 121] [680 164] [681 442] [682 92] [683 249] [684 637] [685 133] [686 345] [687 65] [688 181] [689 493] [690 92] [691 256] [692 49] [693 120] [694 341] [695 66] [696 178] [697 460] [698 95] [699 257] [700 47] [701 132] [702 358] [703 66] [704 187] [705 36] [706 91] [707 260] [708 48] [709 132] [710 134] [711 64] [712 177] [713 33] [714 92] [715 281] [716 42] [717 135] [718 22] [719 65] [720 205] [721 33] [722 102] [723 11] [724 43] [725 141] [726 20] [727 71] [728 204] [729 31] [730 105] [731 11] [732 47] [733 139] [734 21] [735 68] [736 8] [737 33] [738 105] [739 16] [740 48] [741 85] [742 26] [743 78] [744 14] [745 37] [746 109] [747 19] [748 56] [749 9] [750 25] [751 81] [752 10] [753 37] [754 4] [755 16] [756 49] [757 7] [758 21] [759 76] [760 11] [761 39] [762 7] [763 20] [764 53] [765 3] [766 22] [767 1] [768 8] [769 39] [770 3] [771 18] [772 33] [773 4] [774 23] [775 3] [776 8] [777 33] [778 4] [779 16] [780 4] [781 11] [782 29] [783 4] [784 16] [785 6] [786 6] [787 25] [788 2] [789 10] [790 30] [791 6] [792 19] [793 3] [794 12] [795 33] [796 5] [797 16] [798 3] [799 11] [800 26] [801 4] [802 14] [803 26] [804 6] [805 17] [806 3] [807 9] [808 29] [809 7] [810 16] [811 4] [812 8] [813 21] [814 4] [815 10] [816 5] [817 4] [818 13] [819 1] [820 3] [821 17] [822 1] [823 6] [825 1] [826 11] [828 2] [830 1] [831 4] [833 1] [834 10] [836 4] [838 3] [839 8] [840 1] [841 4] [843 2] [844 5] [845 1] [846 3] [847 1] [848 1] [849 6] [851 3] [852 9] [854 4] [856 2] [857 5] [858 1] [859 3] [860 1] [861 2] [862 3] [864 1] [865 3] [867 1] [869 1] [870 2] [871 1] [872 2] [875 1] [880 1] [882 1] [883 2] [885 1] [887 1] [888 3] [890 1] [892 1] [893 2] [894 1] [895 2] [896 4] [897 2] [898 3] [899 1] [900 2] [901 3] [902 1] [903 2] [906 2] [909 1] [914 1] [937 1] [939 1] [940 2] [941 1] [942 2] [943 1] [944 2] [945 4] [946 2] [947 4] [948 2] [949 4] [950 6] [951 3] [952 4] [953 1] [954 3] [955 5] [956 1] [957 3] [958 6] [959 1] [960 3] [962 1] [963 4] [964 1] [965 2] [968 1] [971 2] [976 1] [986 1]])
  (->> data-10-pow-9 (map first)))
