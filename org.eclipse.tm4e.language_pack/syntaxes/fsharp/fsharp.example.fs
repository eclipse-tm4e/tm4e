// Function definition
let greet name =
    sprintf "Hello, %s!" name

// Function invocation
printfn "%s" (greet "User")

// List
let numbers = [1; 2; 3; 4; 5]

// Pattern matching in a function
let rec sumList lst =
    match lst with
    | [] -> 0
    | head :: tail -> head + sumList tail

// Output result
printfn "Sum of numbers: %d" (sumList numbers)

// Record type
type Person = { Name: string; Age: int }

// Create an instance of the record type
let person = { Name = "John"; Age = 30 }

// Pattern matching on record type
let greetPerson p =
    match p with
    | { Name = name; Age = age } -> sprintf "Hello, %s! You are %d years old." name age

// Output result
printfn "%s" (greetPerson person)
