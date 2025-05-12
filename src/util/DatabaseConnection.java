package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Bootstraps an embedded, file-based H2 database with all tables.
 */
public class DatabaseConnection {


    private static final String JDBC_URL =
            "jdbc:h2:file:./bankdb;MODE=MySQL;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        // On class load, connect and create any missing tables.
        try (Connection conn = getConnection();
             Statement  s    = conn.createStatement()) {

            // === users table ===
            s.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  ID            INT AUTO_INCREMENT PRIMARY KEY,
                  first_name    VARCHAR(100),
                  last_name     VARCHAR(100),
                  password_hash VARCHAR(256),
                  username      VARCHAR(100) UNIQUE
                )
            """);

            // === account table ===
            s.execute("""
                CREATE TABLE IF NOT EXISTS account (
                  account_id    INT AUTO_INCREMENT PRIMARY KEY,
                  user_id       INT NOT NULL,
                  account_type  ENUM('CHECKING','SAVING') NOT NULL,
                  balance       DOUBLE NOT NULL DEFAULT 0,
                  interest_rate DOUBLE,
                  FOREIGN KEY (user_id) REFERENCES users(ID)
                )
            """);

            // === transactions table ===
            s.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                  transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
                  account_id       INT NOT NULL,
                  transaction_type ENUM('DEPOSIT','WITHDRAWAL','TRANSFER') NOT NULL,
                  amount           DOUBLE NOT NULL,
                  time_stamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  description      VARCHAR(255),
                  status           ENUM('COMPLETED','FAILED','PENDING')
                                      DEFAULT 'COMPLETED',
                  FOREIGN KEY (account_id) REFERENCES account(account_id)
                )
            """);

        } catch (SQLException e) {
            // Fail fast if we can’t bootstrap our schema
            throw new ExceptionInInitializerError(e);
        }
    }

    /** Get a live JDBC connection to H2. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASS);
    }
}
