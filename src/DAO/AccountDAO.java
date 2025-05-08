package DAO;

import model.Account;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {


    /**
     * Close account from the bank system
     * Also deletes all its transactions first.
     * @param accountID the  account to close
     * @param conn   open Connection (in a transaction)
     * @return true if deleted
     */
    public boolean closeAccount(int accountID, Connection conn) throws SQLException {
        //Make sure the balance is 0 before
        String checkSql = "SELECT balance FROM account WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, accountID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getDouble(1) != 0.0) {
                    return false; // non-zero or missing
                }
            }
        }

        //Remove all transactions from that account in the bank database
        String delTx = "DELETE FROM transactions WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(delTx)) {
            ps.setInt(1, accountID);
            ps.executeUpdate();
        }

        // If all these parameters pass, continue deleting the account
        String delAcct = "DELETE FROM account WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(delAcct)) {
            ps.setInt(1, accountID);
            return ps.executeUpdate() == 1;
        }
    }


    /**
     * Create an account
     * @param acct The account object to insert into the database
     * @param conn The established connection to the database
     * @return true or false on account creation success
     * @throws SQLException ensures proper connection
     */
    public boolean createAccount(Account acct, Connection conn) throws SQLException {

        //Create the sql query
        String sql = """
            INSERT INTO account (user_id, account_type, balance, interest_rate)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, acct.getUSER_ID());
            ps.setString(2, acct.getACCOUNT_TYPE());
            ps.setDouble(3, acct.getBalance());
            //if savings account, also insert interest rate
            if (acct instanceof Account.SavingsAccount sav) {
                ps.setDouble(4, sav.getInterestRate());
            } else {
                // Checking account
                ps.setNull(4, Types.DOUBLE);
            }

            if (ps.executeUpdate() == 0) return false;


            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) acct.setACCOUNT_NUMBER(keys.getInt(1));
            }
            return true;
        }
    }


    /**
     * Return the ID of the checking account for a specific user
     * @param userId the ID linked to their user account
     * @param conn The connection to the database
     * @return the integer value of the checking id
     * @throws SQLException ensures proper connection to sql
     */
    public Integer getCheckingIdForUser(int userId, Connection conn) throws SQLException {
        //Create sqls string to grab linked account id
        String sql = "SELECT account_id FROM account WHERE user_id=? AND account_type='CHECKING' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /**
     * allow the user to deposit money into a specific account
     * @param conn The connection to the database
     * @param accountID The checking account / saving account id
     * @param amt the amount to enter
     * @return true or false based on success
     */
    public boolean deposit(Connection conn, int accountID, double amt) {
        String sql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amt);
            ps.setInt   (2, accountID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Allows the user to withdraw money from their account
     * @param conn the connection to the database
     * @param accountID the account ID to take money from
     * @param amt the amount of money to take out
     * @return true or false based on success
     */
    public boolean withdraw(Connection conn, int accountID, double amt) {
        String sql = """
            UPDATE account
               SET balance = balance - ?
             WHERE account_id = ? AND balance >= ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amt);
            ps.setInt   (2, accountID);
            ps.setDouble(3, amt);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returnf all accounts of a specific user
     * @param userId The user id to check accounts for
     * @return all the users accounts
     * @throws SQLException ensures proper sql connection
     */
    public List<Account> getAccountsByUser(int userId) throws SQLException {
        String sql = """
            SELECT account_id, account_type, balance, interest_rate
              FROM account
             WHERE user_id = ?
            """;

        List<Account> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                //Check for both checkings and savings accounts ot return specific values for each
                while (rs.next()) {
                    int    id   = rs.getInt("account_id");
                    String typ  = rs.getString("account_type");
                    double bal  = rs.getDouble("balance");

                    if ("CHECKING".equals(typ)) {
                        Account.CheckingAccount c =
                                new Account.CheckingAccount(userId, bal);
                        c.setACCOUNT_NUMBER(id);
                        list.add(c);
                    } else {                       // enum value is 'SAVING'
                        double rate = rs.getDouble("interest_rate");
                        Account.SavingsAccount s =
                                new Account.SavingsAccount(userId, bal, rate);
                        s.setACCOUNT_NUMBER(id);
                        list.add(s);
                    }
                }
            }
        }
        return list;
    }
}
