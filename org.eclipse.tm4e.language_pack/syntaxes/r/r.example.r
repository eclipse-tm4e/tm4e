# Variables
name <- "Alice"
age <- 25

# Print
cat("Hello, ", name, "! You are ", age, " years old.\n")

# Vector and Loop
numbers <- c(1, 2, 3, 4, 5)
for (number in numbers) {
  cat(number, "\n")
}

# List
person <- list(name = "Bob", age = 30)

# Matrix
matrix_example <- matrix(1:6, nrow = 2, ncol = 3)

# Function
greet <- function(person) {
  cat("Hello, ", person$name, "! You are ", person$age, " years old.\n")
}

greet(person)

# Data Frame
data_frame <- data.frame(Name = c("John", "Jane"), Age = c(25, 30))

# Plot
plot(numbers, type = "o", main = "Example Plot", xlab = "Index", ylab = "Values")

# Subsetting
subset_data <- data_frame[data_frame$Age > 25, ]

# ifelse statement
label <- ifelse(age > 30, "Senior", "Junior")
cat("Age category:", label, "\n")

# apply function
average_number <- mean(numbers)

# Factors
gender <- c("Male", "Female", "Male", "Female")
gender_factor <- as.factor(gender)

# while loop
i <- 1
while (i <= 3) {
  cat("Iteration", i, "\n")
  i <- i + 1
}

# install and load a package
if (!requireNamespace("ggplot2", quietly = TRUE)) {
  install.packages("ggplot2")
}
library(ggplot2)

# ggplot
ggplot(data_frame, aes(x = Age, y = Name)) +
  geom_point() +
  ggtitle("Scatter Plot")

# Custom function
double <- function(x) {
  return(x * 2)
}

cat("Double of 5:", double(5), "\n")
