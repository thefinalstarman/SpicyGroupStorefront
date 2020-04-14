Export database:
mysqldump -u username --password=your_password database_name > file.sql
Import database:
mysql -u username --password=your_password database_name < file.sql

SCEMA:

CREATE TABLE Products (
    itemId INT NOT NULL AUTO_INCREMENT,
    price DECIMAL (10,2) NOT NULL,
    name VARCHAR (255),
    PRIMARY KEY(itemId));

CREATE TABLE Discounts (
       discountId int NOT NULL AUTO_INCREMENT,
       date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       PRIMARY KEY (discountId));

CREATE TABLE Persons (
       personId INT NOT NULL AUTO_INCREMENT,
       name VARCHAR(255),
       credit VARCHAR(255) NOT NULL,
       billingAddress VARCHAR(255),
       PRIMARY KEY (personId));

CREATE TABLE Credit (
	personId INT NOT NULL,
	name VARCHAR(255),
	number CHAR(16),
	monthExp CHAR(2),
	yearExp CHAR(2),
	zip CHAR(9)
	FOREIGN KEY(personId) REFERENCES Person(personId)3);

CREATE TABLE Orders( 
    orderId INT NOT NULL AUTO_INCREMENT, 
    itemId INT NOT NULL, 
    discountId INT, 
    personId INT NOT NULL, 
    PRIMARY KEY (orderId),
    FOREIGN KEY (itemId) REFERENCES Products(itemId),
    FOREIGN KEY (discountId) REFERENCES Discounts(discountId),
    FOREIGN KEY (personId) REFERENCES Persons(personId));
