package DAO;

import java.sql.*;

import model.Account;

//Author: Ricky Franco
//01 April 2025
//AccountDAO.java: SQL related account operations
public class AccountDAO {

    /**
     * @param account account to add to sql
     * @param connection databsae connection
     * @return true or false based on success
     */
    public boolean createAccount(Account account, Connection connection) throws SQLException {

        String sql = """
            INSERT INTO account (user_id, account_type, balance, interest_rate)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, account.getUSER_ID());
            ps.setString(2, account.getACCOUNT_TYPE());
            ps.setDouble(3, account.getBalance());
            if (account instanceof Account.SavingsAccount) {
                ps.setDouble(4, ((Account.SavingsAccount) account).getInterestRate());
            } else {
                ps.setNull(4, Types.DOUBLE);
            }

            int rows = ps.executeUpdate();
            if (rows == 0) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setACCOUNT_NUMBER(keys.getInt(1));
                }
            }
            return true;
        }
        }



    /**
     * @param accountID Users account id
     * @param newBalance users updated balance
     * @param connection SQL connection
     * @return true or false based on success
     */
    public boolean updateAccountBalance(int accountID, double newBalance, Connection connection){
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param connection SQL connnection
     * @param accountID users account ID
     * @param amount amount to withdraw
     * @return true or false based on success
     */
    public boolean withdraw(Connection connection, int accountID,double amount){
        String sql = "UPDATE account SET balance = balance - ? WHERE account_id = ? AND balance >= ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, accountID);
            ps.setDouble(3, amount);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param connection SQL connection
     * @param accountID users account ID
     * @param amount amount ot deposi t
     * @return true or false based on success
     */
    public boolean deposit(Connection connection, int accountID,double amount){
        String sql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, accountID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
