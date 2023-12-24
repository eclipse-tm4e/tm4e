// Function definition
def greet(name) {
  "Hello, $name!"
}

// Function invocation
println greet("User")

// List
def numbers = [1, 2, 3, 4, 5]

// Closure (anonymous function)
def sumList = { list ->
  list.sum()
}

// Output result
println "Sum of numbers: ${sumList(numbers)}"

// Map
def person = [name: "John", age: 30]

// Accessing map values
println "Name: ${person.name}, Age: ${person.age}"

// Conditional statement
def isAdult(person) {
  if (person.age >= 18) {
    true
  } else {
    false
  }
}

// Output result
println "Is adult? ${isAdult(person)}"

// Class
class Person {
  String name
  int age
}

// Object instantiation
def personObject = new Person(name: "Alice", age: 25)

// Output result
println "Person Object: ${personObject.name}, ${personObject.age}"
