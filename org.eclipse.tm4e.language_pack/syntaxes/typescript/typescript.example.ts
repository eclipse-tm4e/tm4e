// Variables and Constants
let name: string = "Alice";
const age: number = 25;

// Print
console.log(`Hello, ${name}! You are ${age} years old.`);

// Arrays and Loop
let numbers: number[] = [1, 2, 3, 4, 5];
for (const number of numbers) {
  console.log(number);
}

// Tuple
let person: [string, number] = ["Bob", 30];

// Functions
function greet(person: [string, number]): void {
  const [name, age] = person;
  console.log(`Hello, ${name}! You are ${age} years old.`);
}

greet(person);

// Interface
interface Shape {
  width: number;
  height: number;
}

// Object instantiation
const rectangle: Shape = { width: 5, height: 10 };
console.log(`Area of the rectangle: ${rectangle.width * rectangle.height}`);

// Enum
enum Result {
  Success = "Operation succeeded",
  Failure = "Operation failed",
}

// Pattern Matching
const result: Result = Result.Success;
switch (result) {
  case Result.Success:
    console.log(result);
    break;
  case Result.Failure:
    console.log(`Error: ${result}`);
    break;
}

// Union Types
let unionVariable: number | string;
unionVariable = 42;
console.log(`Union variable: ${unionVariable}`);
unionVariable = "Hello, TypeScript!";
console.log(`Union variable: ${unionVariable}`);

// Optional and Default Parameters
function printMessage(message: string, prefix: string = "Info"): void {
  console.log(`[${prefix}] ${message}`);
}

printMessage("This is a message");
printMessage("Another message", "Warning");

// Classes and Inheritance
class Animal {
  private sound: string;

  constructor(sound: string) {
    this.sound = sound;
  }

  makeSound(): void {
    console.log(this.sound);
  }
}

class Dog extends Animal {
  constructor() {
    super("Woof");
  }
}

const dog = new Dog();
dog.makeSound();

// Generics
function identity<T>(arg: T): T {
  return arg;
}

const result1: number = identity(42);
const result2: string = identity("Hello, Generics!");

// Promises
function asyncOperation(): Promise<string> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve("Async operation completed");
    }, 2000);
  });
}

asyncOperation().then((result) => console.log(result));

// Decorators
function log(target: any, key: string): void {
  console.log(`Method ${key} is called`);
}

class ExampleClass {
  @log
  method(): void {
    console.log("Doing something...");
  }
}

const exampleInstance = new ExampleClass();
exampleInstance.method();
