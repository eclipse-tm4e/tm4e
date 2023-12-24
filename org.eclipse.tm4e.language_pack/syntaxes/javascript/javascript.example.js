// Variables and basic data types
let x = 5;
const pi = 3.14;
let message = "JavaScript is awesome!";

// Output variables
console.log(`x: ${x}, pi: ${pi.toFixed(2)}, message: ${message}`);

// Arrays
let fruits = ['Apple', 'Banana', 'Orange'];

// Loop through the array
console.log("Fruits:");
fruits.forEach(fruit => console.log(fruit));

// Objects
let person = {
  name: 'John',
  age: 30,
};

// Output object properties
console.log(`Person: ${person.name}, ${person.age} years old`);

// Functions
function add(a, b) {
  return a + b;
}

// Function invocation
console.log(`Sum of 10 and 20: ${add(10, 20)}`);

// Conditional statement
if (x > 3) {
  console.log("x is greater than 3.");
} else {
  console.log("x is not greater than 3.");
}

// Switch statement
let dayOfWeek = 3;
let dayName;
switch (dayOfWeek) {
  case 1:
  case 2:
  case 3:
  case 4:
  case 5:
    dayName = "Weekday";
    break;
  case 6:
  case 7:
    dayName = "Weekend";
    break;
  default:
    dayName = "Invalid day";
}

console.log(`Day type: ${dayName}`);

// Template literals
let product = 'Laptop';
let price = 1200;

console.log(`Product: ${product}, Price: $${price}`);

// Arrow function
let square = (num) => num * num;
console.log(`Square of 5: ${square(5)}`);
