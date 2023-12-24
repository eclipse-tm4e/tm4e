void main() {
  // Output to console
  print("Hello, Dart!");

  // Variables and basic data types
  int x = 5;
  double pi = 3.14;
  String message = "Dart is awesome!";

  // Output variables
  print("x: $x, pi: $pi, message: $message");

  // Object instantiation
  var person = Person(name: "John", age: 30);

  // Method invocation
  person.printDetails();

  // Lists
  List<int> numbers = [1, 2, 3, 4, 5];

  // Loop through list
  print("Numbers: ${numbers.join(' ')}");

  // Conditional statement
  if (x > 3) {
    print("x is greater than 3.");
  } else {
    print("x is not greater than 3.");
  }
}

// Class definition
class Person {
  String name;
  int age;

  // Constructor
  Person({required this.name, required this.age});

  // Method
  void printDetails() {
    print("Name: $name, Age: $age");
  }
}
