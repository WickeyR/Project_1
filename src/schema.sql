
CREATE TABLE IF NOT EXISTS users (
                                     ID            INT AUTO_INCREMENT PRIMARY KEY,
                                     first_name    VARCHAR(100),
                                     last_name     VARCHAR(100),
                                     password_hash VARCHAR(256),
                                     username      VARCHAR(100) UNIQUE
);

CREATE TABLE IF NOT EXISTS account (
                                       account_id    INT AUTO_INCREMENT PRIMARY KEY,
                                       user_id       INT NOT NULL,
                                       account_type  ENUM('CHECKING','SAVING') NOT NULL,
                                       balance       DOUBLE DEFAULT 0,
                                       interest_rate DOUBLE,
                                       FOREIGN KEY (user_id) REFERENCES users(ID)
);

CREATE TABLE IF NOT EXISTS transactions (
                                            transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
                                            account_id       INT NOT NULL,
                                            transaction_type ENUM('DEPOSIT','WITHDRAWAL','TRANSFER') NOT NULL,
                                            amount           DOUBLE NOT NULL,
                                            time_stamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            description      VARCHAR(255),
                                            status           ENUM('COMPLETED','FAILED','PENDING') DEFAULT 'COMPLETED',
                                            FOREIGN KEY (account_id) REFERENCES account(account_id)
);
