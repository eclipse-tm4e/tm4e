#!/usr/bin/perl

use strict;   # Enable strict mode for better error checking
use warnings; # Enable warnings to get notified about potential issues

# Scalar variables
my $name = "Alice";
my $age = 25;

# Array
my @numbers = (1, 2, 3, 4, 5);

# Hash (associative array)
my %person = (
  name => "Bob",
  age  => 30,
);

# Conditional statement
if ($age < 30) {
  print "$name is young.\n";
} else {
  print "$name is not so young.\n";
}

# Looping through an array
foreach my $number (@numbers) {
  print "$number ";
}
print "\n";

# Looping through a hash
while (my ($key, $value) = each %person) {
  print "$key: $value\n";
}

# Subroutine (function)
sub greet {
  my ($name) = @_;
  print "Hello, $name!\n";
}

# Call the subroutine
greet("Charlie");

# Regular expression
my $sentence = "This is a Perl example.";
if ($sentence =~ /Perl/) {
  print "Perl found in the sentence.\n";
}

# File I/O
open(my $file, '>', 'output.txt') or die "Could not open file: $!";
print $file "This is written to a file.\n";
close($file);

# Command-line arguments
my $arg1 = $ARGV[0] // "default";
print "Command-line argument: $arg1\n";

# Here document
my $text = <<"END";
This is a
multiline
string.
END
print $text;

# Hash reference
my $person_ref = {
  name => "David",
  age  => 35,
};

# Accessing hash reference values
print "Name: $person_ref->{name}, Age: $person_ref->{age}\n";

# Module usage (DateTime module)
use DateTime;
my $dt = DateTime->now;
print "Current date and time: $dt\n";
