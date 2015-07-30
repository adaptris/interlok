//
// NOTE : this script uses the delimiter="#" (changed because of ant task requirements)
// if you are to use this script therefore, you should change the delimitier manually
//
// like :
// SET delimiter #
//
// and afterwards :
// SET delimiter ;
//

IF object_id('liverpool_transfers', 'U') is not null
  drop table liverpool_transfers#
  
if object_id('one_in') is not null
	drop procedure one_in#
  
if object_id('many_in') is not null
	drop procedure many_in#
	
if object_id('one_out') is not null
	drop procedure one_out#

if object_id('many_out') is not null
	drop procedure many_out#

if object_id('one_inout') is not null
	drop procedure one_inout#

if object_id('many_inout') is not null
	drop procedure many_inout#

if object_id('one_in_one_out') is not null
	drop procedure one_in_one_out#

if object_id('many_in_many_out') is not null
	drop procedure many_in_many_out#

if object_id('one_inout_one_in') is not null
	drop procedure one_inout_one_in#

if object_id('many_inout_many_in') is not null
	drop procedure many_inout_many_in#

if object_id('one_inout_one_out') is not null
	drop procedure one_inout_one_out#

if object_id('many_inout_many_out') is not null
	drop procedure many_inout_many_out#
	
if object_id('one_inout_one_in_one_out') is not null
	drop procedure one_inout_one_in_one_out#

if object_id('many_inout_many_in_many_out') is not null
	drop procedure many_inout_many_in_many_out#

if object_id('one_resultset') is not null
	drop procedure one_resultset#

if object_id('many_resultsets') is not null
	drop procedure many_resultsets#

if object_id('out_in_in_in') is not null
	drop procedure out_in_in_in#
	

CREATE TABLE liverpool_transfers (
  id int IDENTITY PRIMARY KEY,
  type VARCHAR(30) NOT NULL,
  player VARCHAR(255) NOT NULL,
  club VARCHAR(255) NOT NULL,
  amount INT NOT NULL,
  manager VARCHAR(255) NOT NULL,
  date DATETIME NOT NULL,
)#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('a', 'b', 'c',1,'d','2000-01-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Djibril Cisse', 'AJ Auxerre',14500000,'Gerard Houllier','2004-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Emile Heskey', 'Leicester City',11000000,'Gerard Houllier','2000-03-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Xabi Alonso', 'Real Sociedad',10700000,'Rafael Benitez','2004-08-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'El-Hadji Diouf', 'RC Lens',10000000,'Gerard Houllier','2002-06-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Stan Collymore', 'Nottingham Forest F.C.',8500000,'Roy Evans','1995-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Dietmar Hamann', 'Newcastle United F.C.',8000000,'Gerard Houllier','1999-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Peter Crouch', 'Southampton F.C.',7000000,'Rafael Benitez','2005-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Jermaine Pennant', 'Birmingham City',6700000,'Rafael Benitez','2006-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Fernando Morientes', 'Real Madrid',6300000,'Rafael Benitez','2005-01-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Nick Barmby', 'Everton F.C.',6000000,'Gerard Houllier','2000-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Chris Kirkland', 'Coventry City F.C.',6000000,'Gerard Houllier','2001-08-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Luis Garcia', 'FC Barcelona',6000000,'Rafael Benitez','2004-08-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Pepe Reina', 'Villarreal CF',6000000,'Rafael Benitez','2005-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Craig Bellamy', 'Blackburn Rovers F.C.',6000000,'Rafael Benitez','2006-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Purchased', 'Daniel Agger', 'Brøndby IF',5800000,'Rafael Benitez','2006-01-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Robbie Fowler', 'Leeds United F.C.',12750000,'Gerard Houllier','2001-11-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Michael Owen', 'Real Madrid',8000000,'Rafael Benitez','2004-08-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Stan Collymore', 'Aston Villa FC',7000000,'Roy Evans','1997-05-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Milan Baroš', 'Aston Villa FC',6500000,'Rafael Benitez','2005-08-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Emile Heskey', 'Birmingham City',6250000,'Gerard Houllier','2004-05-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Dominic Matteo', 'Leeds United F.C.',4750000,'Gerard Houllier','2000-08-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Jason McAteer', 'Blackburn Rovers F.C.',4000000,'Gerard Houllier','1999-01-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Christian Ziege', 'Tottenham Hotspur F.C.',4000000,'Gerard Houllier','2001-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Sander Westerveld', 'Real Sociedad',3750000,'Gerard Houllier','2001-12-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Nick Barmby', 'Leeds United F.C.',3750000,'Gerard Houllier','2002-08-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'El-Hadji Diouf', 'Bolton Wanderers F.C.',3500000,'Rafael Benitez','2005-06-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Ian Rush', 'Juventus',3200000,'Kenny Dalglish','1987-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Stephen Wright', 'Sunderland A.F.C.',3000000,'Gerard Houllier','2002-08-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Fernando Morientes', 'Valencia CF',3000000,'Rafael Benitez','2006-07-01 00:00:00')#
INSERT INTO liverpool_transfers (type, player, club, amount, manager, date) VALUES ('Sold', 'Øyvind Leonhardsen', 'Tottenham Hotspur F.C.',2800000,'Gerard Houllier','1999-08-01 00:00:00')#


CREATE PROCEDURE no_params
 AS
  SELECT COUNT(*) as 'number_of_transfers' FROM liverpool_transfers;#


 
 CREATE PROCEDURE out_in_in_in @cashAmount INT OUTPUT, @xType varchar(30), @playerName VARCHAR(255), @clubName VARCHAR(255)
 AS
	SELECT @cashAmount = amount from liverpool_transfers where type = @xType and player = @playerName and club = @clubName;#
 


CREATE PROCEDURE one_in @xType varchar(30)
 AS
  SELECT COUNT(*) as 'number_of_transfers' FROM liverpool_transfers WHERE type = @xType;#




CREATE PROCEDURE many_in @xType varchar(30), @xManager varchar(255)
 AS
  SELECT COUNT(*) as 'number_manager_sold' FROM liverpool_transfers WHERE type = @xType AND manager = @xManager;#




CREATE PROCEDURE one_out @transferCount INT OUTPUT
 AS
  SELECT @transferCount = COUNT(*) FROM liverpool_transfers;#




CREATE PROCEDURE many_out @numberSold INT OUTPUT, @numberPurchased INT OUTPUT, @totalTransfers INT OUTPUT
 AS
  SELECT @numberSold = COUNT(*) FROM liverpool_transfers WHERE type = 'Sold';
  SELECT @numberPurchased = COUNT(*) FROM liverpool_transfers WHERE type = 'Purchased';
  SELECT @totalTransfers = COUNT(*) FROM liverpool_transfers;#




CREATE PROCEDURE one_inout @xSomeAmount INT OUTPUT
 AS
   DECLARE @agentsCosts INT;
   SET @agentsCosts = (@xSomeAmount / 100) * 5;
   SET @xSomeAmount = @xSomeAmount + @agentsCosts;#




CREATE PROCEDURE many_inout @xSomeAmount INT OUTPUT, @xManagerName VARCHAR(255) OUTPUT
 AS
   SET @xSomeAmount = @xSomeAmount + ((@xSomeAmount / 100) * 5);
   select top 1 @xManagerName = player from liverpool_transfers where manager = @xManagerName order by amount desc;#




CREATE PROCEDURE one_in_one_out @xType varchar(30), @transferCount INT OUTPUT
 AS
  SELECT @transferCount = COUNT(*)FROM liverpool_transfers WHERE type = @xType;#




CREATE PROCEDURE many_in_many_out @xManagerName varchar(255), @xDateFrom datetime, @xDateTo datetime, @transferType VARCHAR(30) OUTPUT, @playerName VARCHAR(255) OUTPUT
 AS
  select top 1 @playerName = player, @transferType = type
  from liverpool_transfers
  where manager = @xManagerName
  and date > @xDateFrom
  and date < @xDateTo
  order by amount desc;#




CREATE PROCEDURE one_inout_one_in @xAgentsAmount INT OUTPUT, @xManagerName varchar(255)
 AS
   DECLARE @playerCost INT;

   select top 1 @playerCost = amount
   from liverpool_transfers
   where manager = @xManagerName
   order by amount desc;

   SET @xAgentsAmount = @xAgentsAmount + @playerCost;#




CREATE PROCEDURE many_inout_many_in @xAgentsAmount INT OUTPUT, @xTaxAmount INT OUTPUT, @xManagerName varchar(255), @xDateFrom datetime, @xDateTo datetime
 AS
   DECLARE @playerCost INT;
   DECLARE @agentsCosts INT;
   DECLARE @taxCosts INT;

   select top 1 @playerCost = amount
   from liverpool_transfers
   where manager = @xManagerName
   and date > @xDateFrom
   and date < @xDateTo
   order by amount desc;

   SET @agentsCosts = (@playerCost / 100) * @xAgentsAmount;
   SET @xAgentsAmount = @agentsCosts;

   SET @taxCosts = (@playerCost / 100) * @xTaxAmount;
   SET @xTaxAmount = @taxCosts;#





CREATE PROCEDURE one_inout_one_out @xAmount INT OUTPUT, @playersName VARCHAR(255) OUTPUT
 AS
   DECLARE @playerCost INT;

   select top 1 @playersName = player, @playerCost = amount
   from liverpool_transfers
   where amount < @xAmount
   order by amount desc;

   SET @xAmount = @playerCost;#




CREATE PROCEDURE many_inout_many_out @xAmount INT OUTPUT, @agentsCost INT OUTPUT, @playersName VARCHAR(255) OUTPUT, @transferDate datetime OUTPUT
 AS
   DECLARE @playerCost INT;

   select top 1 @playersName = player, @playerCost = amount, @transferDate = date
   from liverpool_transfers
   where amount < @xAmount
   order by amount desc;

   SET @xAmount = @playerCost;
   SET @agentsCost = (@playerCost / 100) * @agentsCost;#




CREATE PROCEDURE one_inout_one_in_one_out @xAmount INT OUTPUT, @managersName VARCHAR(255), @playersName VARCHAR(255) OUTPUT
 AS
   DECLARE @playerCost INT;

   select top 1 @playersName = player, @playerCost = amount
   from liverpool_transfers
   where amount < @xAmount
   and manager = @managersName
   order by amount desc;

   SET @xAmount = @playerCost;#




CREATE PROCEDURE many_inout_many_in_many_out @xAmount INT OUTPUT, @agentsCost INT OUTPUT, @managersName VARCHAR(255), @transferType VARCHAR(255), @playersName VARCHAR(255) OUTPUT, @transferDate datetime OUTPUT
 AS
   DECLARE @playerCost INT;

   select top 1 @playersName = player, @playerCost = amount, @transferDate = date
   from liverpool_transfers
   where amount < @xAmount
   and manager = @managersName
   and type = @transferType
   order by amount desc;

   SET @xAmount = @playerCost;
   SET @agentsCost = (@playerCost / 100) * @agentsCost;#




CREATE PROCEDURE one_resultset
 AS
   select top 5 player, amount, date
   from liverpool_transfers
   order by amount desc;#




CREATE PROCEDURE many_resultsets
 AS
   select top 5 player, amount, date
   from liverpool_transfers
   where type = 'Sold'
   order by amount desc;

   select top 5 player, amount, date
   from liverpool_transfers
   where type = 'Purchased'
   order by amount desc;#

