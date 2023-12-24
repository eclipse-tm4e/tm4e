// Variables and Constants
fn main() {
  let name = "Alice";
  let age = 25;
  const PI: f64 = 3.14159;

  // Print
  println!("Hello, {}! You are {} years old.", name, age);

  // Arrays and Iteration
  let numbers = [1, 2, 3, 4, 5];
  for number in &numbers {
     println!("{}", number);
  }

  // Vectors and Iteration
  let numbers_vec = vec![1, 2, 3, 4, 5];
  for number in &numbers_vec {
     println!("{}", number);
  }

  // Tuple
  let person = ("Bob", 30);
  println!("Name: {}, Age: {}", person.0, person.1);

  // Struct
  struct Rectangle {
    width: u32,
    height: u32,
  }

  // Object instantiation and Method
  let rectangle = Rectangle { width: 5, height: 10 };
  println!("Area of the rectangle: {}", rectangle.area());

  // Enum
  enum Shape {
    Rectangle(Rectangle),
    Circle(f64),
  }

  // Pattern Matching
  let shape = Shape::Rectangle(Rectangle { width: 3, height: 4 });
  match shape {
    Shape::Rectangle(rect) => println!("Area of the rectangle: {}", rect.area()),
    Shape::Circle(radius) => println!("Area of the circle: {}", circle_area(radius)),
  }

  // Function with Generic Type
  fn generic_function<T>(value: T) {
    println!("Generic value: {:?}", value);
  }

  generic_function(42);
  generic_function("Rust");

  // Ownership and Borrowing
  let text = String::from("Hello, Rust!");
  take_ownership(text);
  // Uncommenting the line below would result in a compilation error
  // println!("After ownership: {}", text);

  // References and Mutable Borrowing
  let mut counter = 0;
  increment(&mut counter);
  println!("Counter after increment: {}", counter);

  // Option Type
  let result = divide(10, 2);
  match result {
    Some(value) => println!("Result: {}", value),
    None => println!("Cannot divide by zero!"),
  }

  // Lifetime Annotation
  fn longest<'a>(s1: &'a str, s2: &'a str) -> &'a str {
    if s1.len() > s2.len() {
      s1
    } else {
      s2
    }
  }

  let string1 = String::from("Rust");
  let string2 = String::from("Programming");
  let result = longest(&string1, &string2);
  println!("Longest string: {}", result);

  // Pattern Matching and Match Guards
  let number = Some(5);
  match number {
    Some(n) if n > 0 => println!("Positive number: {}", n),
    Some(n) if n < 0 => println!("Negative number: {}", n),
    _ => println!("Not a valid number"),
  }
}

// Method Implementation for Rectangle
impl Rectangle {
  fn area(&self) -> u32 {
    self.width * self.height
  }
}

// Function for Circle Area
fn circle_area(radius: f64) -> f64 {
  PI * radius * radius
}

// Function for Ownership Transfer
fn take_ownership(s: String) {
  println!("Owned value: {}", s);
}

// Function for Mutable Borrowing
fn increment(counter: &mut i32) {
  *counter += 1;
}

// Function with Option Type
fn divide(x: i32, y: i32) -> Option<i32> {
  if y != 0 {
    Some(x / y)
  } else {
    None
  }
}
