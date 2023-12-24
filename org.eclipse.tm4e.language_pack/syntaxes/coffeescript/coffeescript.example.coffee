# Function definition
greet = (name) ->
  "Hello, #{name}!"

# Function invocation
console.log greet "User"

# Object literals
person =
  name: "John"
  age: 30
  city: "Example City"

# Accessing object properties
console.log "Name: #{person.name}"
console.log "Age: #{person.age}"

# Conditionals
isAdult = (person) ->
  if person.age >= 18
    true
  else
    false

# Looping - for...in
numbers = [1, 2, 3, 4, 5]
for number in numbers
  console.log "Number: #{number}"

# Recursion
factorial = (n) ->
  if n <= 1
    1
  else
    n * factorial(n - 1)

console.log "Factorial of 5: #{factorial 5}"
