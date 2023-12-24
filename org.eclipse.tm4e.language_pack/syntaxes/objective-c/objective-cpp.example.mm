#import <Foundation/Foundation.h>
#import <iostream>

// Define a C++ class
class Car {
public:
  // Constructor
  Car(std::string make, int year) : make(make), year(year) {}

  // Member function
  void displayInfo() {
    std::cout << year << " " << make << " car." << std::endl;
  }

private:
  std::string make;
  int year;
};

// Define an Objective-C class
@interface Person : NSObject

// Properties
@property (nonatomic, strong) NSString *name;
@property (nonatomic, assign) NSInteger age;

// Class method
+ (void)sayHello;

// Instance method
- (void)introduce;

@end

@implementation Person

// Implement the class methods
+ (void)sayHello {
  NSLog(@"Hello from the Person class!");
}

// Implement the instance methods
- (void)introduce {
  NSLog(@"I am %@ and I am %ld years old.", self.name, (long)self.age);
}

@end

int main(int argc, const char * argv[]) {
  @autoreleasepool {
    // Use C++ class
    Car myCar("Toyota", 2022);
    myCar.displayInfo();

    // Use Objective-C class
    Person *person = [[Person alloc] init];
    person.name = @"John";
    person.age = 30;

    // Call class method
    [Person sayHello];

    // Call instance method
    [person introduce];
  }
  return 0;
}
