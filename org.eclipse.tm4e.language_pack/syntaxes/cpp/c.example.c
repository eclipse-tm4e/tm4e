#include <stdio.h>

// Function declaration
int addNumbers(int a, int b);

int main() {
  // Output to console
  printf("Hello, C!\n");

  // Variable declaration and initialization
  int x = 5;
  int y = 3;

  // Function call
  int sum = addNumbers(x, y);

  // Output result
  printf("Sum: %d\n", sum);

  // Array
  int numbers[] = {1, 2, 3, 4, 5};

  // Loop through array
  printf("Numbers: ");
  for (int i = 0; i < sizeof(numbers) / sizeof(numbers[0]); ++i) {
    printf("%d ", numbers[i]);
  }
  printf("\n");

  // Conditional statement
  if (sum > 5) {
    printf("Sum is greater than 5.\n");
  } else {
    printf("Sum is not greater than 5.\n");
  }

  // Return statement
  return 0;
}

// Function definition
int addNumbers(int a, int b) {
  return a + b;
}
