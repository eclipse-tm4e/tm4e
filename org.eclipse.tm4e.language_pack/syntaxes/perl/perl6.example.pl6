#!/usr/bin/env perl6

# Scalar variables
my $name = "Alice";
my $age = 25;

# Array
my @numbers = 1, 2, 3, 4, 5;

# Hash (associative array)
my %person = name => "Bob", age => 30;

# Conditional statement
if $age < 30 {
  say "$name is young.";
} else {
  say "$name is not so young.";
}

# Looping through an array
for @numbers -> $number {
  print "$number ";
}
say "";

# Looping through a hash
for %person.kv -> $key, $value {
  say "$key: $value";
}

# Subroutine (multi-sub)
multi sub greet($name) {
  say "Hello, $name!";
}

# Call the subroutine
greet("Charlie");

# Regex match
my $sentence = "This is a Perl 6 example.";
say "Perl 6 found in the sentence." if $sentence ~~ /Perl 6/;

# File I/O
my $file = open 'output.txt', :w;
$file.say("This is written to a file.");
$file.close;

# Command-line arguments
my $arg1 = @*ARGS[0] // "default";
say "Command-line argument: $arg1";

# Here document
my $text = q:to/END/;
  This is a
  multiline
  string.
END
say $text;

# Hash reference
my $person_ref = {
  name => "David",
  age  => 35,
};

# Accessing hash reference values
say "Name: $person_ref<name>, Age: $person_ref<age>";

# Module usage (DateTime module)
use DateTime;
my $dt = DateTime.now;
say "Current date and time: $dt";
