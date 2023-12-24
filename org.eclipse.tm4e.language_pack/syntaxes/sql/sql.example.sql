-- Create a database
CREATE DATABASE IF NOT EXISTS ExampleDB;

-- Use the database
USE ExampleDB;

-- Create a table
CREATE TABLE IF NOT EXISTS Persons (
  PersonID INT PRIMARY KEY,
  FirstName VARCHAR(50),
  LastName VARCHAR(50),
  Age INT
);

-- Insert data into the table
INSERT INTO Persons (PersonID, FirstName, LastName, Age)
VALUES
  (1, 'Alice', 'Johnson', 25),
  (2, 'Bob', 'Smith', 30),
  (3, 'Charlie', 'Brown', 22);

-- Select data from the table
SELECT * FROM Persons;

-- Update data
UPDATE Persons
SET Age = 26
WHERE PersonID = 1;

-- Delete data
DELETE FROM Persons
WHERE PersonID = 2;

-- Joins
CREATE TABLE IF NOT EXISTS Orders (
  OrderID INT PRIMARY KEY,
  PersonID INT,
  Product VARCHAR(50),
  Quantity INT
);

INSERT INTO Orders (OrderID, PersonID, Product, Quantity)
VALUES
  (101, 1, 'Apples', 5),
  (102, 3, 'Bananas', 8);

-- Inner Join
SELECT Persons.FirstName, Orders.Product
FROM Persons
INNER JOIN Orders ON Persons.PersonID = Orders.PersonID;

-- Aggregation
SELECT PersonID, COUNT(OrderID) AS OrderCount
FROM Orders
GROUP BY PersonID;

-- Subqueries
SELECT FirstName
FROM Persons
WHERE PersonID IN (SELECT PersonID FROM Orders WHERE Quantity > 5);

-- Indexing
CREATE INDEX idx_last_name ON Persons (LastName);

-- Transactions
START TRANSACTION;
INSERT INTO Persons (PersonID, FirstName, LastName, Age) VALUES (4, 'David', 'Miller', 28);
COMMIT;

-- Stored Procedures
DELIMITER //
CREATE PROCEDURE GetPerson(IN personId INT)
BEGIN
  SELECT * FROM Persons WHERE PersonID = personId;
END //
DELIMITER ;

-- Call the stored procedure
CALL GetPerson(3);
