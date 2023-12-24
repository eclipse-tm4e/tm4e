import Foundation

// Variables and Constants
var name = "Alice"
let age = 25

// Print
print("Hello, \(name)! You are \(age) years old.")

// Arrays and Loop
let numbers = [1, 2, 3, 4, 5]
for number in numbers {
  print(number)
}

// Dictionary
let person = ["name": "Bob", "age": 30]

// Functions
func greet(person: [String: Any]) {
  if let name = person["name"], let age = person["age"] {
    print("Hello, \(name)! You are \(age) years old.")
  }
}

greet(person: person)

// Struct
struct Rectangle {
  var width: Double
  var height: Double

  func area() -> Double {
    return width * height
  }
}

// Object instantiation
let rectangle = Rectangle(width: 5.0, height: 10.0)
print("Area of the rectangle: \(rectangle.area())")

// Enum
enum Result {
  case success(String)
  case failure(String)
}

// Pattern Matching
let result: Result = .success("Operation succeeded")
switch result {
case .success(let message):
  print(message)
case .failure(let message):
  print("Error: \(message)")
}

// Optionals
var optionalName: String? = "Optional"
if let name = optionalName {
  print("The name is \(name)")
} else {
  print("Name is nil")
}

// Classes and Inheritance
class Vehicle {
  var brand: String

  init(brand: String) {
    self.brand = brand
  }

  func startEngine() {
    print("Engine started.")
  }
}

class Car: Vehicle {
  var model: String

  init(brand: String, model: String) {
    self.model = model
    super.init(brand: brand)
  }

  override func startEngine() {
    print("Car engine started.")
  }
}

let car = Car(brand: "Toyota", model: "Camry")
car.startEngine()

// Protocols
protocol Speaker {
  func speak()
}

// Extensions
extension String: Speaker {
  func speak() {
    print("Speaking: \(self)")
  }
}

"Hello, Swift!".speak()

// Generics
func printArray<T>(array: [T]) {
  for element in array {
    print(element)
  }
}

let stringArray = ["Apple", "Banana", "Orange"]
let intArray = [1, 2, 3, 4, 5]

printArray(array: stringArray)
printArray(array: intArray)

// Error Handling
enum FileError: Error {
  case notFound
  case permissionDenied
}

func readFile() throws {
  throw FileError.notFound
}

do {
  try readFile()
} catch FileError.notFound {
  print("File not found.")
} catch FileError.permissionDenied {
  print("Permission denied.")
} catch {
  print("An unexpected error occurred.")
}

// Higher-Order Functions
let numbersSquared = numbers.map { $0 * $0 }
print("Squared numbers: \(numbersSquared)")

let evenNumbers = numbers.filter { $0 % 2 == 0 }
print("Even numbers: \(evenNumbers)")

let sum = numbers.reduce(0, +)
print("Sum of numbers: \(sum)")
