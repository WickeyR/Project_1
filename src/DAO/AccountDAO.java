package DAO;

import model.Account;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    /* ---------- create ---------- */
    public boolean createAccount(Account acct, Connection conn) throws SQLException {
        String sql = """
            INSERT INTO account (user_id, account_type, balance, interest_rate)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, acct.getUSER_ID());
            ps.setString(2, acct.getACCOUNT_TYPE());
            ps.setDouble(3, acct.getBalance());
            if (acct instanceof Account.SavingsAccount sav) {
                ps.setDouble(4, sav.getInterestRate());
            } else {
                ps.setNull(4, Types.DOUBLE);
            }

            if (ps.executeUpdate() == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) acct.setACCOUNT_NUMBER(keys.getInt(1));
            }
            return true;
        }
    }

    /* ---------- balance update helpers ---------- */
    public boolean updateAccountBalance(int acctId, double newBal, Connection conn) {
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBal);
            ps.setInt   (2, acctId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Integer getCheckingIdForUser(int userId, Connection conn) throws SQLException {
        String sql = "SELECT account_id FROM account WHERE user_id=? AND account_type='CHECKING' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public boolean deposit(Connection conn, int acctId, double amt) {
        String sql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amt);
            ps.setInt   (2, acctId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean withdraw(Connection conn, int acctId, double amt) {
        String sql = """
            UPDATE account
               SET balance = balance - ?
             WHERE account_id = ? AND balance >= ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amt);
            ps.setInt   (2, acctId);
            ps.setDouble(3, amt);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ---------- fetch all accounts for a user ---------- */
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
