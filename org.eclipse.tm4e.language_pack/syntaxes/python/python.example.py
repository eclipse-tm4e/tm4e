# Variables
name = "Alice"
age = 25

# Print
print(f"Hello, {name}! You are {age} years old.")

# List and Loop
numbers = [1, 2, 3, 4, 5]
for number in numbers:
    print(number)

# Dictionary
person = {'name': 'Bob', 'age': 30}

# Tuple
point = (10, 20)


# Function
def greet(person):
    print(f"Hello, {person['name']}! You are {person['age']} years old.")


greet(person)


# Class
class Rectangle:

    def __init__(self, width, height):
        self.width = width
        self.height = height

    def area(self):
        return self.width * self.height


# Object instantiation
rectangle = Rectangle(5, 10)
print(f"Area of the rectangle: {rectangle.area()}")

# Exception handling
try:
    result = 10 / 0
except ZeroDivisionError:
    print("Cannot divide by zero!")

# File I/O
with open('example.txt', 'w') as file:
    file.write('This is a sample file.')

with open('example.txt', 'r') as file:
    content = file.read()
    print("File content:", content)

# List comprehension
squared_numbers = [x ** 2 for x in range(1, 6)]
print("Squared numbers:", squared_numbers)

# Lambda function
double = lambda x: x * 2
print("Double of 5:", double(5))

# Set
unique_numbers = {1, 2, 3, 3, 4, 5}
print("Unique numbers:", unique_numbers)
