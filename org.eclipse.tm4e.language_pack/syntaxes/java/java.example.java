import java.util.List;
import static java.util.stream.Collectors.*;

public final class Example {

  // Enum type for days of the week
  enum Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
  }

  // Record class for representing a person
  record Person(String name, int age) {
  }

  public static void main(final String[] args) {
    // Variables and basic data types
    int x = 5;
    double pi = 3.14;
    String message = "Java is awesome!";

    // Output variables
    System.out.printf("x: %d, pi: %.2f, message: %s%n", x, pi, message);

    // Collections (List) with type inference
    var fruits = List.of("Apple", "Banana", "Orange");

    // Enhanced for loop using var
    System.out.print("Fruits: ");
    for (var fruit : fruits) {
       System.out.print(fruit + " ");
    }
    System.out.println();

    // Conditional statement with pattern matching
    if (x > 3) {
      System.out.println("x is greater than 3.");
    } else {
      System.out.println("x is not greater than 3.");
    }

    // Switch expression
    Day today = Day.WEDNESDAY;
    String dayType = switch (today) {
      case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
      case SATURDAY, SUNDAY -> "Weekend";
    };

    System.out.println("Day type: " + dayType);

    // Record instantiation and deconstruction
    Person person = new Person("Alice", 25);
    System.out.printf("Person: %s, %d years old%n", person.name(), person.age());

    // Multiline string
    String multilineString = """
      This is a multiline string
      spanning multiple lines.
      It's a great addition in Java 17!
      """;

    System.out.println(multilineString);

    // Static imports
    List<Integer> numbers = List.of(1, 2, 3, 4, 5);
    int sum = numbers.stream().collect(summarizingInt(Integer::intValue)).getSum();
    System.out.println("Sum of numbers: " + sum);
  }
}
