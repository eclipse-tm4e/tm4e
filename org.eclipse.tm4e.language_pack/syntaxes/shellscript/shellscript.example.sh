#!/bin/bash

# Variables
name="Alice"
age=25

# Print
echo "Hello, $name! You are $age years old."

# Arrays and Loop
numbers=(1 2 3 4 5)
for number in "${numbers[@]}"; do
  echo $number
done

# Functions
greet() {
  local person=$1
  echo "Hello, $person!"
}

greet "Bob"

# Conditions
if [ $age -lt 30 ]; then
  echo "$name is young."
else
  echo "$name is not so young."
fi

# File I/O
echo "This is written to a file." > output.txt
content=$(<output.txt)
echo "File content: $content"

# Command-line arguments
arg1=${1:-"default"}
echo "Command-line argument: $arg1"

# Here document
cat <<EOF
This is a
multiline
string.
EOF

# Arithmetic operations
result=$((5 + 7))
echo "Arithmetic result: $result"

# Case statement
fruit="apple"
case $fruit in
  "apple")
     echo "It's an apple.";;
  "banana")
     echo "It's a banana.";;
  *) echo "Unknown fruit.";;
esac

# While loop
i=0
while [ $i -lt 3 ]; do
  echo "Iteration $((i + 1))"
  ((i++))
done

# Process substitution
diff <(sort file1.txt) <(sort file2.txt)

# Command substitution
current_date=$(date)
echo "Current date and time: $current_date"

# Brace expansion
echo {1..5}

# Arrays and associative arrays
colors=("red" "green" "blue")
declare -A person
person=( ["name"]="David" ["age"]=35 )

# String manipulation
string="Hello, World!"
echo "Length of the string: ${#string}"

# Pattern matching
if [[ $name =~ [A-Z][a-z]+ ]]; then
  echo "$name matches the pattern."
fi

# Exit status
command_that_fails
if [ $? -ne 0 ]; then
  echo "The previous command failed."
fi
