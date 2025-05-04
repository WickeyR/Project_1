package DAO;

import model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /**
     * @param amount dollar amount of the transaction
     * @return true or false based on success
     */
    public boolean logTransaction(Connection conn,
                                  int accountId,
                                  String type,        // "DEPOSIT","WITHDRAWAL","TRANSFER"
                                  double amount,
                                  String desc,        // nullable
                                  String status)      // "COMPLETED" by default
            throws SQLException {

        String sql = """
            INSERT INTO transactions
              (account_id, transaction_type, amount, time_stamp, description, status)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, accountId);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setString(4, desc);
            ps.setString(5, status);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * @param acctId AccountId associated with the transatcions
     * @return a list of transactions
     */
    public List<Transaction> getTransactionsByAccount(int acctId, int limit) throws SQLException {
        String sql = """
            SELECT transaction_id, transaction_type, amount,
                   time_stamp, description, status
              FROM transactions
             WHERE account_id = ?
             ORDER BY time_stamp DESC
             LIMIT ?
            """;
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = util.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, acctId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("transaction_id"),
                            acctId,
                            rs.getString("transaction_type"),
                            rs.getDouble("amount"),
                            rs.getTime("time_stamp"),
                            rs.getString("description"),
                            rs.getString("status")));
                }
            }
        }
        return list;
    }



}
