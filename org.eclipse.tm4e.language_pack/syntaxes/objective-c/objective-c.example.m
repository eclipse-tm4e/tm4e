#import <Foundation/Foundation.h>

// Define a simple class
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
    // Create an instance of the Person class
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
