Export database:
mysqldump -u username --password=your_password database_name > file.sql
Import database:
mysql -u username --password=your_password database_name < file.sql

SCEMA:

CREATE TABLE Products (
    itemId INT NOT NULL AUTO_INCREMENT,
    price DECIMAL (10,2) NOT NULL,
    name VARCHAR (255) NOT NULL,
    PRIMARY KEY(itemId));

CREATE TABLE Discounts (
       discountId int NOT NULL AUTO_INCREMENT,
       dateCreated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       exp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       PRIMARY KEY (discountId));

CREATE TABLE Persons (
       personId INT NOT NULL AUTO_INCREMENT,
       name VARCHAR(255),
       billingAddress VARCHAR(255),
       PRIMARY KEY (personId));

CREATE TABLE Credit (
creditId INT NOT NULL AUTO_INCREMENT,
	personId INT NOT NULL,
	name VARCHAR(255) NOT NULL,
	number VARCHAR(16) NOT NULL,
	monthExp CHAR(2) NOT NULL,
	cvv CHAR(3) NOT NULL,
	yearExp CHAR(2) NOT NULL,
	zip CHAR(9) NOT NULL,
	FOREIGN KEY(personId) REFERENCES Persons(personId),
	PRIMARY KEY (creditId));

CREATE TABLE Orders( 
    orderId INT NOT NULL AUTO_INCREMENT, 
    itemId INT NOT NULL, 
    discountId INT,
    creditId INT NOT NULL,
    personId INT NOT NULL, 
    PRIMARY KEY (orderId),
    FOREIGN KEY (itemId) REFERENCES Products(itemId),
    FOREIGN KEY (discountId) REFERENCES Discounts(discountId),
    FOREIGN KEY (personId) REFERENCES Persons(personId),
    FOREIGN KEY (creditId) REFERENCES Credit(creditId));

TRIGGERS:

CREATE TRIGGER creditcheck BEFORE INSERT ON Orders
FOR EACH ROW
BEGIN
IF
NEW.personId <> (SELECT C.personId FROM Credit as C WHERE C.creditId = NEW.creditId)
THEN
SIGNAL SQLSTATE ‘45000’ SET MESSAGE_TEXT = ‘creditId does not match personId’;
END IF;
END;

CREATE TRIGGER discountcheck BEFORE INSERT ON Orders
FOR EACH ROW
BEGIN
IF
CURRENT_TIME > DATE(DATE_ADD((SELECT D.dateCreated FROM Discounts as D WHERE D.discountId = NEW.discountId), INTERVAL 7 DAY))
THEN
SIGNAL SQLSTATE ‘45000’ SET MESSAGE_TEXT = ‘discount expired’

CREATE TRIGGER discountexp BEFORE INSERT ON Discounts
FOR EACH ROW
BEGIN
SET NEW.exp = DATE(DATE_ADD(NEW.dateCreated, INTERVAL 7 DAY));
END;

