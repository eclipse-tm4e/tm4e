package main

import (
    "fmt"
    "math"
)

// Struct definition
type Circle struct {
    radius float64
}

// Method on a struct
func(c Circle) area() float64 {
    return math.Pi * c.radius * c.radius
}

// Function with multiple return values
func add(a, b int)(int, error) {
    sum: = a + b
    return sum,
    nil
}

func main() {
    // Output to console
    fmt.Println("Hello, Go!")

    // Variables and basic data types
    x: = 5
    y: = 3.14
    message: = "Go is awesome!"

    // Output variables
    fmt.Printf("x: %d, y: %f, message: %s\n", x, y, message)

    // Create an instance of a struct
    c: = Circle {
        radius: 3.0
    }

    // Call a method on a struct
    circleArea: = c.area()
    fmt.Printf("Area of the circle: %f\n", circleArea)

    // Function invocation with multiple return values
    result, err: = add(10, 20)
    if err != nil {
        fmt.Println("Error:", err)
    } else {
        fmt.Println("Sum:", result)
    }
}