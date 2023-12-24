#include <iostream>
#include <vector>

// Function declaration
int addNumbers(int a, int b);

int main() {
  // Output to console
  std::cout << "Hello, C++!" << std::endl;

  // Variable declaration and initialization
  int x = 5;
  int y = 3;

  // Function call
  int sum = addNumbers(x, y);

  // Output result
  std::cout << "Sum: " << sum << std::endl;

  // Vector (dynamic array)
  std::vector<int> numbers = {1, 2, 3, 4, 5};

  // Loop through vector
  std::cout << "Numbers: ";
  for (int num : numbers) {
    std::cout << num << " ";
  }
  std::cout << std::endl;

  // Conditional statement
  if (sum > 5) {
    std::cout << "Sum is greater than 5." << std::endl;
  } else {
    std::cout << "Sum is not greater than 5." << std::endl;
  }

  // Return statement
  return 0;
}

// Function definition
int addNumbers(int a, int b) {
  return a + b;
}
