# Comments
# This is a comment

# Variables
name = "John"
age = 25

# String interpolation
puts "My name is #{name} and I am #{age} years old."

# Arrays
numbers = [1, 2, 3, 4, 5]

# Iteration
numbers.each do |number|
  puts "Number: #{number}"
end

# Hashes
person = { name: "Alice", age: 30, city: "Wonderland" }

# Symbols
key = :name

# Accessing hash values
puts "#{person[key]} lives in #{person[:city]}."

# Conditionals
if age >= 18
  puts "I am an adult."
else
  puts "I am a minor."
end

# Methods
def greet(name)
  puts "Hello, #{name}!"
end

greet("Bob")

# Classes and objects
class Dog
  attr_accessor :name, :age

  def initialize(name, age)
    @name = name
    @age = age
  end

  def bark
    puts "Woof!"
  end
end

# Create an instance of Dog
my_dog = Dog.new("Buddy", 3)

# Method invocation
my_dog.bark

# Exception handling
begin
  result = 10 / 0
rescue ZeroDivisionError => e
  puts "Error: #{e.message}"
end
