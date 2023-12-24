<?php

// Variables and Data Types
$name = "John";
$age = 30;
$isStudent = false;
$grades = [85, 90, 78];

// Control Structures
if ($age >= 18) {
    echo "Welcome, $name! You are eligible.";
} else {
    echo "Sorry, $name. You are not eligible.";
}

// Loops
for ($i = 0; $i < count($grades); $i++) {
    echo "Grade $i: {$grades[$i]}";
}

// Functions
function greet($name) {
    return "Hello, $name!";
}

echo greet($name);

// Arrays
$colors = ["red", "green", "blue"];
$associativeArray = [
    "name" => "Jane",
    "age" => 25,
    "isStudent" => true
];

// Classes and Objects
class Person {
    public $name;
    public $age;

    public function __construct($name, $age) {
        $this->name = $name;
        $this->age = $age;
    }

    public function greet() {
        echo "Hello, my name is $this->name.";
    }
}

$person = new Person("Alice", 22);
$person->greet();

// Traits
trait Logging {
    public function log($message) {
        echo "Logging: $message";
    }
}

// Namespaces
namespace MyApp;

// Error Handling
try {
    // Code that might throw an exception
    throw new Exception("Something went wrong!");
} catch (Exception $e) {
    echo "Caught exception: " . $e->getMessage();
}

// File Handling
$fileContent = file_get_contents("example.txt");
file_put_contents("output.txt", $fileContent);

// Regular Expressions
$pattern = "/\d{2}-\d{2}-\d{4}/";
$dateString = "12-31-2022";

if (preg_match($pattern, $dateString)) {
    echo "Valid date format.";
} else {
    echo "Invalid date format.";
}
?>