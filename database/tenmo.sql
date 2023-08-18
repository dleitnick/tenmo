BEGIN TRANSACTION;

DROP TABLE IF EXISTS transfer, account, tenmo_user, transfer_type, transfer_status;
DROP SEQUENCE IF EXISTS seq_user_id, seq_account_id, seq_transfer_id;


CREATE TABLE transfer_type (
	transfer_type_id serial NOT NULL,
	transfer_type_desc varchar(10) NOT NULL,
	CONSTRAINT PK_transfer_type PRIMARY KEY (transfer_type_id)
);

CREATE TABLE transfer_status (
	transfer_status_id serial NOT NULL,
	transfer_status_desc varchar(10) NOT NULL,
	CONSTRAINT PK_transfer_status PRIMARY KEY (transfer_status_id)
);

CREATE SEQUENCE seq_user_id
  INCREMENT BY 1
  START WITH 1001
  NO MAXVALUE;

CREATE TABLE tenmo_user (
	user_id int NOT NULL DEFAULT nextval('seq_user_id'),
	username varchar(50) UNIQUE NOT NULL,
	password_hash varchar(200) NOT NULL,
	role varchar(20),
	CONSTRAINT PK_tenmo_user PRIMARY KEY (user_id),
	CONSTRAINT UQ_username UNIQUE (username)
);

CREATE SEQUENCE seq_account_id
  INCREMENT BY 1
  START WITH 2001
  NO MAXVALUE;

CREATE TABLE account (
	account_id int NOT NULL DEFAULT nextval('seq_account_id'),
	user_id int NOT NULL,
	balance decimal(13, 2) NOT NULL,
	is_primary boolean NOT NULL,
	is_deleted boolean NOT NULL,
	CONSTRAINT PK_account PRIMARY KEY (account_id),
	CONSTRAINT FK_account_tenmo_user FOREIGN KEY (user_id) REFERENCES tenmo_user (user_id)
);

CREATE SEQUENCE seq_transfer_id
  INCREMENT BY 1
  START WITH 3001
  NO MAXVALUE;

CREATE TABLE transfer (
	transfer_id int NOT NULL DEFAULT nextval('seq_transfer_id'),
	transfer_type_id int NOT NULL,
	transfer_status_id int NOT NULL,
	account_from int NOT NULL,
	account_to int NOT NULL,
	amount decimal(13, 2) NOT NULL,
	transfer_created timestamp NOT NULL,
	transfer_completed timestamp,
	CONSTRAINT PK_transfer PRIMARY KEY (transfer_id),
	CONSTRAINT FK_transfer_account_from FOREIGN KEY (account_from) REFERENCES account (account_id),
	CONSTRAINT FK_transfer_account_to FOREIGN KEY (account_to) REFERENCES account (account_id),
	CONSTRAINT FK_transfer_transfer_status FOREIGN KEY (transfer_status_id) REFERENCES transfer_status (transfer_status_id),
	CONSTRAINT FK_transfer_transfer_type FOREIGN KEY (transfer_type_id) REFERENCES transfer_type (transfer_type_id),
	CONSTRAINT CK_transfer_not_same_account CHECK (account_from <> account_to),
	CONSTRAINT CK_transfer_amount_gt_0 CHECK (amount > 0)
);

INSERT INTO transfer_status (transfer_status_desc) VALUES ('Pending');
INSERT INTO transfer_status (transfer_status_desc) VALUES ('Approved');
INSERT INTO transfer_status (transfer_status_desc) VALUES ('Rejected');

INSERT INTO transfer_type (transfer_type_desc) VALUES ('Request');
INSERT INTO transfer_type (transfer_type_desc) VALUES ('Send');
INSERT INTO transfer_type (transfer_type_desc) VALUES ('Move');

COMMIT;

BEGIN;
INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount, transfer_created, transfer_completed) 
VALUES (2, 2, 2002, 2001, 500.00, NOW(), NOW()) 
RETURNING transfer_id, transfer_completed;
INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount, transfer_created, transfer_completed) 
VALUES (1, 1, 2001, 2002, 500.00, NOW(), NOW()) 
RETURNING transfer_id, transfer_completed;

--Adding new accounts
INSERT INTO account (user_id, balance, is_primary)
VALUES (1002, 0.00, false);
UPDATE account SET is_primary = false WHERE user_id = 1002;
UPDATE account SET is_primary = true WHERE account_id = 2002;
ROLLBACK;

SELECT * FROM tenmo_user;
SELECT * FROM transfer;
SELECT * FROM transfer_status;
SELECT * FROM transfer_type;
SELECT * FROM account;

--Get all accounts and their usernames
SELECT a.account_id, a.user_id, u.username, a.balance, a.is_primary
FROM account a
JOIN tenmo_user u ON u.user_id = a.user_id
WHERE u.username = 'daniel';

--Delete account and move balance to primary
BEGIN;
UPDATE account 
SET balance = (SELECT balance FROM account WHERE account_id = 2002) + 
(SELECT balance FROM account WHERE account_id = 2005)
WHERE account_id = 2002;
UPDATE account
SET balance = 0, is_deleted = true,
WHERE account_id = 2005;
ROLLBACK;

SELECT * FROM transfer
WHERE 2001 IN (account_from, account_to)
AND transfer_type_id IN (1, 2, 3)
AND transfer_status_id IN (1, 2, 3);

SELECT u.username
FROM account a
JOIN tenmo_user u ON u.user_id = a.user_id
WHERE a.account_id = 2001;

SELECT * FROM transfer WHERE transfer_id = 3031;
SELECT * FROM transfer WHERE 2010 IN (account_from, account_to);

SELECT user_id, username, password_hash FROM tenmo_user WHERE username != 'joey' ORDER BY username;
