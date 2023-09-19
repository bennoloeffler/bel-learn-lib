(ns bel-learn-chapters.x-150-transducers
  (:require [clojure.core.async :as a]))


; READ THIS CAREFULLY: https://dev.solita.fi/2021/10/14/grokking-clojure-transducers.html
; AND THIS: https://functional.works-hub.com/learn/a-mental-model-for-thinking-about-clojures-transducers-6baba
; AND THIS: https://andreyorst.gitlab.io/posts/2022-08-13-understanding-transducers/
; AND THIS: page 88, FILE: 050 Pragmatic.Clojure.Applied.From.Practice.to.Practitioner.pdf
; AND THIS: https://eli.thegreenplace.net/2017/reducers-transducers-and-coreasync-in-clojure/

; MACRO for creating transducers (parallel) from threading
; https://github.com/johnmn3/injest

(comment ; what is "abstraction" in a functional way?
  ; we have a function...
  ; that reduces two collections each by adding
  ; and multiplies those results.
  (defn add-multiply [coll1 coll2]
    (let [val1 (reduce + coll1)
          val2 (reduce + coll2)]
      (* val1 val2)))

  ; another function may be needed:
  (defn add-add [coll1 coll2]
    (let [val1 (reduce + coll1)
          val2 (reduce + coll2)]
      (+ val1 val2)))

  ; maybe, we have an EXPLOSION of functions
  ; that do permutations of e.g.
  ; + * -, for the first step:
  ; 1. reducing the two collections
  ; then we have many functions to
  ; 2. combine those two values to one, eg: + - * /

  ; so we may create a function that
  ; receives the reduction function
  ; and the combination function.
  (defn reduce-and-combine [coll1 coll2]
    (fn [reduce-f, combine-f]
      (let [val1 (reduce reduce-f coll1)
            val2 (reduce reduce-f coll2)]
        (combine-f val1 val2))))

  (def f (reduce-and-combine [1 2 3] [4 5 6]))

  (add-multiply [1 2 3] [4 5 6])
  (f + ; first reduce with +
     *) ; then combine with *
  (f * *)
  (f * -)
  (f - +)

  ;; What did we do?
  ;; We created a function that closes over
  ;; coll1 and coll2 and receives reduce-f and
  ;; combine-f as parameter.
  ;; We abstracted away the inner dependency from
  ;; the reducing-function and the combining-function

  ;; Transducers do the opposite:
  ;; They abstract away the access to the unterlying
  ;; data source

  nil)

(comment

  (def nums (range 10))
  (defn square [n] (* n n))

  ; reducing - nums and transformation tied together
  (reduce + 0 (->> nums
                   (filter odd?)
                   (map square)))

  ; now with transducer, transformation before reducing
  (transduce (comp (filter odd?)
                   (map square)) + 0 nums)

  ; a transducer may be defined seperately
  (def xf (comp (filter odd?)
                (map square)))

  ; the transducer (transformation pipeline) as parameter
  (transduce xf + 0 nums)

  ; BUT: you may not debug, e.g. with debux/dbg

  nil)


(comment

  ; combined function - applied only once. No intermediate sequence.
  (def xform
    ; create transducer (ommiting the col)
    (comp (map inc)
          (filter even?)))

  ; apply transducer
  (->> (range 10)
       (sequence xform)
       (prn "result is "))


  ; now with core.async
  (do
    (def result (a/chan))

    ; a channel uses transducer to transform its input
    (def transformed (->> (a/pipe result (a/chan 10 xform))
                          (a/into [])))

    (a/go (prn "result is " (a/<! transformed)))
    (a/go (doseq [n (range 10)]
            (a/>! result n))
          (a/close! result))))


; https://eli.thegreenplace.net/2017/reducers-transducers-and-coreasync-in-clojure/
(comment

  (def s (range 0 10))
  (reduce + (map inc (filter even? s)))

  ;
  ; reducingf :: acc -> item -> acc
  ;

  ; map inc by reduce
  (reduce (fn [acc item] (conj acc (inc item))) [] [1 2 3 4 5])

  ; filter even? by reduce
  (reduce (fn [acc item] (if (even? item)
                           (conj acc item)
                           acc))
          [] [1 2 3 4 5])

  ;
  ; transformingf :: (acc -> item -> acc) -> (acc -> item -> acc)
  ;
  (defn mapping-transform
    [mapf]
    (fn [reducingf]
      (fn [acc item]
        (reducingf acc (mapf item)))))

  (reduce ((mapping-transform #(* % %)) +) 0 [1 2 3 4 5 6])

  (defn filtering-transform
    [predicate]
    (fn [reducingf]
      (fn [acc item]
        (if (predicate item)
          (reducingf acc item)
          acc))))

  (reduce ((filtering-transform even?) +) 0 [1 2 3 4 5 6])

  (reduce ((filtering-transform even?)
           ((mapping-transform inc) +)) 0 (range 0 10))

  ; in clojure, with transducers
  (def sv (vec (range 1000000)))
  (reduce ((comp (filter even?) (map inc)) +) 0 sv)

  ; so again:
  (def s (range 0 10))
  (reduce + (map inc (filter even? s)))
  ;; the transformation is NOT reusable

  ;; here, it is...
  (reduce ((comp (filter even?) (map inc)) +) 0 sv)
  (def xform (comp (filter even?) (map inc)))
  (reduce (xform +) 0 sv))

(comment ; from the ground...

  ; what is a reducing function and why does it need arities?
  (reduce + []) ; rf needs arity-0 to get initial value
  (reduce + 99 []) ; rf needs nothing...
  (reduce + 100 [1 2 3]) ; rf needs arity-2

  (defn rf-arity-0 [] 0.0)
  (defn rf-arity-0-2
    ([] 0.0)
    ([acc val] (+ acc val)))
  (reduce rf-arity-0 [])
  (reduce rf-arity-0 100 [])
  (reduce rf-arity-0 100 [1]) ; FAILS, need arity-2
  (reduce rf-arity-0 [1]) ; works - just return first
  (reduce rf-arity-0 [1 2]) ; fails

  (reduce rf-arity-0-2 100 [1])
  (reduce rf-arity-0-2 [1 2])


  ;https://andreyorst.gitlab.io/posts/2022-08-13-understanding-transducers/

  ; write a map in terms of reduce
  (defn map-r [f coll]
    (reduce
      (fn [acc val]
        (conj acc (f val)))
      []
      coll))
  (map-r inc [2 3 4])

  ; write a filter in terms of reduce
  (defn filter-r [p coll]
    (reduce
      (fn [acc val]
        (if (p val)
          (conj acc val)
          acc))
      []
      coll))
  (filter-r even? [2 3 4])

  ;; make it independant of conj and reduce
  ;; instead of
  (reduce
    (fn [acc val]
      (conj acc (inc val)))
    []
    [1 2 3])
  ;; we write a function, that provides the reducer (conj)
  ;; as parameter and delivers a reducing function with
  ;; this reducer in place
  (fn [reducer]
    (fn [acc val]
      (reducer acc (inc val))))

  ;; finally, the mapping function f - which is inc - needs
  ;; to be abstracted:
  (defn map-transducer [f]
    (fn [reducer]
      (fn [acc val]
        (reducer acc (f val)))))

  ; we can do exactry the same with filter
  (defn filter-transducer [p]
    (fn [reducer]
      (fn [acc val]
        (if (p val)
          (reducer acc val)
          acc))))

  ; how to use this?
  (def incrementer (map-transducer inc)) ; teach inc (f)
  (def incer-coll (incrementer conj)) ; teach conj (reducer)
  (reduce incer-coll [] [1 2 3])

  ; use filter-transducer
  (def even-finder (filter-transducer even?))
  (def e-fdr-coll (even-finder conj))
  (reduce e-fdr-coll [] [1 2 3 4])

  ; compose transducers
  (def t (comp (map-transducer inc)
               (filter-transducer even?)))

  (reduce (t conj) [] [99 0 -99 2 4 6])

  ;; give the transducer the reduction function +
  (def t-add-nums (t +)) ; teach +
  (reductions t-add-nums -10 [1 2 3 4 5 6]) ; steps
  (reduce t-add-nums -10 [1 2 3 4 5 6]) ; result

  (partition-all 3 (range 8)) ; a regular partition-all call

  ;; what about state in a transducer?
  (defn partition-all-transducer [n]
    (fn [reducer]
      (let [next-partition (atom [])]
        (fn [acc val]
          (swap! next-partition conj val)
          (if (= (count @next-partition) n)
            (let [np-tmp @next-partition]
              (reset! next-partition [])
              (reducer acc np-tmp))
            acc)))))
  (def part-trcer (partition-all-transducer 3))
  (def pt-coll (part-trcer conj))
  (reduce pt-coll [] (range 8))

  ;; ??? what about the [6 7] ?
  ;; completition step is missing...
  (defn partition-all-transducer [n]
    (fn [reducer]
      (let [next-partition (atom [])]
        (fn
          ([acc] ;; *** second arity: completes ********
           (if (pos? (count @next-partition))
             (reducer acc @next-partition)
             (reducer acc)))
          ([acc val] ;; this is unchanged
           (swap! next-partition conj val)
           (if (= (count @next-partition) n)
             (let [np-tmp @next-partition]
               (reset! next-partition [])
               (reducer acc np-tmp))
             acc))))))

  (def part-trcer (partition-all-transducer 3))
  (let [pt-coll (part-trcer conj)
        result  (reduce pt-coll [] (range 8))]
    (pt-coll result)) ; call competing function

  ;; this step of completing is called transduce
  (defn my-transduce
    ([xform f coll]
     (my-transduce xform f (f) coll))
    ([xform f init coll] ;; this is the core of transduce
     (let [f   (xform f)
           ret (reduce f init coll)]
       (f ret))))

  ;; the transducer is called xform
  ;; the reducing function is called f
  (my-transduce part-trcer conj [] (range 8))

  ;; transduce exists in clojure.core
  (transduce part-trcer conj [] (range 8))
  (transduce part-trcer conj []) ;; works already ?

  ;; finally, if there is no initialization value here ,,,,,
  ;; the reducing function may be called without
  ;; initial value. Therefore, there are 3 arities:
  ;; ,,,,, without init value, e.g. []
  (let [pt-coll (part-trcer conj)
        result  (reduce pt-coll,,,,,, (range 8))]
    (pt-coll result)) ; call competing function
  ; => ERROR
  ; may be solved with r/reduce
  (let [pt-coll (part-trcer conj)
        result  (clojure.core.reducers/reduce pt-coll (range 8))]
    (pt-coll result))
  ; NO. arity missing in reducer function

  (defn partition-all-transducer-2 [n]
    (fn [reducer]
      (let [next-partition (atom [])]
        (fn
          ([] ;; ***** third arity: creates initial value *****
           (reducer))
          ([acc]
           (if (pos? (count @next-partition))
             (reducer acc @next-partition)
             (reducer acc)))
          ([acc val] ;; this is unchanged
           (swap! next-partition conj val)
           (if (= (count @next-partition) n)
             (let [np-tmp @next-partition]
               (reset! next-partition [])
               (reducer acc np-tmp))
             acc))))))

  (def part-trcer-2 (partition-all-transducer-2 3))
  (let [pt-coll (part-trcer-2 conj)
        result  (reduce pt-coll,,,,, (range 8))]
    (pt-coll result)) ; call competing function
  ; => still ERROR

  (let [pt-coll (part-trcer-2 conj)
        result  (clojure.core.reducers/reduce pt-coll,,,,, (range 8))]
    (pt-coll result)) ; call competing function
  ; => but this works

  ;; part-trcer may be used in core.async channel

  nil)

;; https://functional.works-hub.com/learn/a-mental-model-for-thinking-about-clojures-transducers-6baba
(comment
  ; a reducing function has a signature like this:
  ; acc, val -> acc
  ; a transducer receives a reducing function
  ; and returns another one.
  ; (acc, val -> acc) -> (acc, val -> acc)

  ; basic idea: reducers
  (defn r-map [mapping-f coll]
    (reduce (fn [acc val]
              (conj acc (mapping-f val)))
            []
            coll))

  (defn r-filter [predicate coll]
    (reduce (fn [acc val]
              (if (predicate val)
                (conj acc val)
                acc))
            []
            coll))


  nil)