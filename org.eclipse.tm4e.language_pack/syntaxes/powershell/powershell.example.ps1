# Variables
$name = "Alice"
$age = 25

# Arrays
$numbers = 1, 2, 3, 4, 5

# Hash table
$person = @{
  Name = "Bob"
  Age  = 30
}

# Conditional statement
if ($age -lt 30) {
  Write-Host "$name is young."
} else {
  Write-Host "$name is not so young."
}

# Looping through an array
foreach ($number in $numbers) {
  Write-Host $number
}

# Looping through a hash table
foreach ($key in $person.Keys) {
  Write-Host "$key: $($person[$key])"
}

# Function
function Greet($name) {
  Write-Host "Hello, $name!"
}

# Call the function
Greet "Charlie"

# Regular expression match
$sentence = "This is a PowerShell example."
if ($sentence -match "PowerShell") {
  Write-Host "PowerShell found in the sentence."
}

# File I/O
Set-Content -Path "output.txt" -Value "This is written to a file."

# Command-line arguments
$arg1 = $args[0] -or "default"
Write-Host "Command-line argument: $arg1"

# Here string
$text = @"
This is a
multiline
string.
"@
Write-Host $text

# Hashtable reference
$personRef = @{
  Name = "David"
  Age  = 35
}

# Accessing hashtable reference values
Write-Host "Name: $($personRef.Name), Age: $($personRef.Age)"

# Module usage (Get-Date module)
$date = Get-Date
Write-Host "Current date and time: $date"
