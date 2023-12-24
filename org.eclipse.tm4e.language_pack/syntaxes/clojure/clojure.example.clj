;; Function definition
(defn greet [name]
  (str "Hello, " name "!"))

;; Function invocation
(println (greet "User"))

;; Data structures - Vector
(def numbers [1 2 3 4 5])

;; Map
(def person {:name "John" :age 30 :city "Example City"})

;; Accessing map values
(println (str "Name: " (:name person)))
(println (str "Age: " (:age person)))

;; Conditionals
(defn is-adult? [person]
  (if (>= (:age person) 18)
    true
    false))

;; Looping - for
(for [number numbers]
  (println (str "Number: " number)))

;; Recursion
(defn factorial [n]
  (if (<= n 1)
    1
    (* n (factorial (- n 1)))))

(println (str "Factorial of 5: " (factorial 5)))