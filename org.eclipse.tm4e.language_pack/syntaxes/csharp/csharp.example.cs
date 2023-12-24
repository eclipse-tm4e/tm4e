using System;

class Program
{
  // Define a class with properties and methods
  class Person
  {
    public string Name { get; set; }
    public int Age { get; set; }

    public void PrintDetails()
    {
       Console.WriteLine($"Name: {Name}, Age: {Age}");
    }
  }

  static void Main()
  {
    // Output to console
    Console.WriteLine("Hello, C#!");

    // Variables and basic data types
    int x = 5;
    double pi = 3.14;
    string message = "C# is awesome!";

    // Output variables
    Console.WriteLine($"x: {x}, pi: {pi}, message: {message}");

    // Object instantiation
    Person person = new Person { Name = "John", Age = 30 };

    // Method invocation
    person.PrintDetails();

    // Arrays
    int[] numbers = { 1, 2, 3, 4, 5 };

    // Loop through array
    Console.Write("Numbers: ");
    foreach (int number in numbers)
    {
      Console.Write($"{number} ");
    }
    Console.WriteLine();

    // Conditional statement
    if (x > 3)
    {
      Console.WriteLine("x is greater than 3.");
    }
    else
    {
      Console.WriteLine("x is not greater than 3.");
    }
  }
}
