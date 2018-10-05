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

DROP TABLE IF EXISTS liverpool_transfers#
DROP PROCEDURE IF EXISTS no_params#
DROP PROCEDURE IF EXISTS one_in#
DROP PROCEDURE IF EXISTS many_in#
DROP PROCEDURE IF EXISTS one_out#
DROP PROCEDURE IF EXISTS many_out#
DROP PROCEDURE IF EXISTS one_inout#
DROP PROCEDURE IF EXISTS many_inout#
DROP PROCEDURE IF EXISTS one_in_one_out#
DROP PROCEDURE IF EXISTS many_in_many_out#
DROP PROCEDURE IF EXISTS one_inout_one_in#
DROP PROCEDURE IF EXISTS many_inout_many_in#
DROP PROCEDURE IF EXISTS one_inout_one_out#
DROP PROCEDURE IF EXISTS many_inout_many_out#
DROP PROCEDURE IF EXISTS one_inout_one_in_one_out#
DROP PROCEDURE IF EXISTS many_inout_many_in_many_out#
DROP PROCEDURE IF EXISTS one_resultset#
DROP PROCEDURE IF EXISTS many_resultsets#
DROP PROCEDURE IF EXISTS out_in_in_in#
DROP PROCEDURE IF EXISTS one_resultset_one_out#

CREATE TABLE liverpool_transfers (
  id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  type VARCHAR(30) NOT NULL,
  player VARCHAR(255) NOT NULL,
  club VARCHAR(255) NOT NULL,
  amount INT NOT NULL,
  manager VARCHAR(255) NOT NULL,
  date DATETIME NOT NULL,
  PRIMARY KEY(id)
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


CREATE PROCEDURE no_params ()
 BEGIN
  SELECT COUNT(*) as 'number_of_transfers' FROM liverpool_transfers;
 END;
 #


 
 CREATE PROCEDURE out_in_in_in (OUT cashAmount INT, IN xType varchar(30), IN playerName VARCHAR(255), IN clubName VARCHAR(255))
 BEGIN
	SELECT amount INTO cashAmount from liverpool_transfers where type = xType and player = playerName and club = clubName;
 END;
 #
 


CREATE PROCEDURE one_in (IN xType varchar(30))
 BEGIN
  SELECT COUNT(*) as 'number_of_transfers' FROM liverpool_transfers WHERE type = xType;
 END;
 #




CREATE PROCEDURE many_in (IN xType varchar(30), IN xManager varchar(255))
 BEGIN
  SELECT COUNT(*) as 'number_manager_sold' FROM liverpool_transfers WHERE type = xType AND manager = xManager;
 END;
 #




CREATE PROCEDURE one_out (OUT transferCount INT)
 BEGIN
  SELECT COUNT(*) INTO transferCount FROM liverpool_transfers;
 END;
 #




CREATE PROCEDURE many_out (OUT numberSold INT, OUT numberPurchased INT, OUT totalTransfers INT)
 BEGIN
  SELECT COUNT(*) INTO numberSold FROM liverpool_transfers WHERE type = 'Sold';
  SELECT COUNT(*) INTO numberPurchased FROM liverpool_transfers WHERE type = 'Purchased';
  SELECT COUNT(*) INTO totalTransfers FROM liverpool_transfers;
 END;
 #




CREATE PROCEDURE one_inout (INOUT xSomeAmount INT)
 BEGIN
   DECLARE agentsCosts INT;
   SET agentsCosts = (xSomeAmount / 100) * 5;
   SET xSomeAmount = xSomeAmount + agentsCosts;
 END;
 #




CREATE PROCEDURE many_inout (INOUT xSomeAmount INT, INOUT xManagerName VARCHAR(255))
 BEGIN
   SET xSomeAmount = xSomeAmount + ((xSomeAmount / 100) * 5);
   select player INTO xManagerName from liverpool_transfers where manager = xManagerName order by amount desc limit 1;
 END;
 #




CREATE PROCEDURE one_in_one_out (IN xType varchar(30), OUT transferCount INT)
 BEGIN
  SELECT COUNT(*) INTO transferCount FROM liverpool_transfers WHERE type = xType;
 END;
 #




CREATE PROCEDURE many_in_many_out (IN xManagerName varchar(255), IN xDateFrom datetime, IN xDateTo datetime, OUT transferType VARCHAR(30), OUT playerName VARCHAR(255))
 BEGIN
  select player, type INTO playerName, transferType
  from liverpool_transfers
  where manager = xManagerName
  and date > xDateFrom
  and date < xDateTo
  order by amount
  desc limit 1;
 END;
 #




CREATE PROCEDURE one_inout_one_in (INOUT xAgentsAmount INT, IN xManagerName varchar(255))
 BEGIN
   DECLARE playerCost INT;

   select amount INTO playerCost
   from liverpool_transfers
   where manager = xManagerName
   order by amount
   desc limit 1;

   SET xAgentsAmount = xAgentsAmount + playerCost;
 END;
 #




CREATE PROCEDURE many_inout_many_in (INOUT xAgentsAmount INT, INOUT xTaxAmount INT, IN xManagerName varchar(255), IN xDateFrom datetime, IN xDateTo datetime)
 BEGIN
   DECLARE playerCost INT;
   DECLARE agentsCosts INT;
   DECLARE taxCosts INT;

   select amount INTO playerCost
   from liverpool_transfers
   where manager = xManagerName
   and date > xDateFrom
   and date < xDateTo
   order by amount
   desc limit 1;

   SET agentsCosts = (playerCost / 100) * xAgentsAmount;
   SET xAgentsAmount = agentsCosts;

   SET taxCosts = (playerCost / 100) * xTaxAmount;
   SET xTaxAmount = taxCosts;
 END;
 #





CREATE PROCEDURE one_inout_one_out (INOUT xAmount INT, OUT playersName VARCHAR(255))
 BEGIN
   DECLARE playerCost INT;

   select player, amount INTO playersName, playerCost
   from liverpool_transfers
   where amount < xAmount
   order by amount
   desc limit 1;

   SET xAmount = playerCost;
 END;
 #




CREATE PROCEDURE many_inout_many_out (INOUT xAmount INT, INOUT agentsCost INT, OUT playersName VARCHAR(255), OUT transferDate datetime)
 BEGIN
   DECLARE playerCost INT;

   select player, amount, date INTO playersName, playerCost, transferDate
   from liverpool_transfers
   where amount < xAmount
   order by amount
   desc limit 1;

   SET xAmount = playerCost;
   SET agentsCost = (playerCost / 100) * agentsCost;
 END;
 #




CREATE PROCEDURE one_inout_one_in_one_out (INOUT xAmount INT, IN managersName VARCHAR(255), OUT playersName VARCHAR(255))
 BEGIN
   DECLARE playerCost INT;

   select player, amount INTO playersName, playerCost
   from liverpool_transfers
   where amount < xAmount
   and manager = managersName
   order by amount
   desc limit 1;

   SET xAmount = playerCost;
 END;
 #




CREATE PROCEDURE many_inout_many_in_many_out (INOUT xAmount INT, INOUT agentsCost INT, IN managersName VARCHAR(255), IN transferType VARCHAR(255), OUT playersName VARCHAR(255), OUT transferDate datetime)
 BEGIN
   DECLARE playerCost INT;

   select player, amount, date INTO playersName, playerCost, transferDate
   from liverpool_transfers
   where amount < xAmount
   and manager = managersName
   and type = transferType
   order by amount
   desc limit 1;

   SET xAmount = playerCost;
   SET agentsCost = (playerCost / 100) * agentsCost;
 END;
 #




CREATE PROCEDURE one_resultset ()
 BEGIN
   select player, amount, date
   from liverpool_transfers
   order by amount
   desc limit 5;
 END;
 #

CREATE PROCEDURE one_resultset_one_out (out completed INT)
 BEGIN
   select player, amount, date
   from liverpool_transfers
   order by amount
   desc limit 5;

   SET completed = 1;
 END;
 # 


CREATE PROCEDURE many_resultsets ()
 BEGIN
   select player, amount, date
   from liverpool_transfers
   where type = 'Sold'
   order by amount
   desc limit 5;

   select player, amount, date
   from liverpool_transfers
   where type = 'Purchased'
   order by amount
   desc limit 5;
 END;
 #

