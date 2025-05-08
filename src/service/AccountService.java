package service;

import DAO.AccountDAO;
import DAO.TransactionDAO;
import DAO.UserDAO;
import model.Account;
import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class AccountService {

    private static final double DEFAULT_SAVINGS_RATE = 0.04;   // 4 %



    /**
     * Transfer funds from one user's checking → another's.
     *
     * @param fromUsername sender's username
     * @param toUsername   recipient's username
     * @param amount       amount to transfer
     * @return true if successful
     */
    public boolean transfer(String fromUsername,
                            String toUsername,
                            double amount) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            UserDAO userDao       = new UserDAO();
            AccountDAO acctDao    = new AccountDAO();
            TransactionDAO txDao  = new TransactionDAO();

            // Look up both users of the transfer
            User sender    = userDao.getUserByUsername(fromUsername);
            User recipient = userDao.getUserByUsername(toUsername);
            if (sender == null || recipient == null) {
                throw new SQLException("Sender or recipient not found");
            }

            //Grab both sender and receiver id
            Integer senderChk    = acctDao.getCheckingIdForUser(sender.getUserId(), conn);
            Integer recipientChk = acctDao.getCheckingIdForUser(recipient.getUserId(), conn);
            if (senderChk == null || recipientChk == null) {
                throw new SQLException("Missing checking account");
            }

            //Take money out of the senders account
            if (!acctDao.withdraw(conn, senderChk, amount)) {
                conn.rollback();
                return false; // insufficient funds
            }

            //Deposit that money into the receivers account
            if (!acctDao.deposit(conn, recipientChk, amount)) {
                conn.rollback();
                return false;
            }

            //Log the transactions for both users
            txDao.logTransaction(conn,
                    senderChk,
                    "TRANSFER",
                    amount,
                    "Sent to " + toUsername,
                    "COMPLETED");
            txDao.logTransaction(conn,
                    recipientChk,
                    "TRANSFER",
                    amount,
                    "Received from " + fromUsername,
                    "COMPLETED");

            conn.commit();
            return true;
        }
    }



    /**
     * Close one of the user’s accounts (must be zero-balance).
     * @param userId the owner
     * @param acctId the account to close
     * @return true if succeeded
     */
    public boolean closeAccount(int userId, int acctId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            AccountDAO dao = new AccountDAO();

            // ensure it belongs to this user:
            boolean owns = dao.getAccountsByUser(userId)
                    .stream()
                    .anyMatch(a -> a.getACCOUNT_NUMBER() == acctId);
            if (!owns) {
                conn.rollback();
                return false;
            }

            // attempt closure
            boolean ok = dao.closeAccount(acctId, conn);
            if (!ok) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Allows uer to open savings account
     * @param userId the user to open the account for
     * @param checkingAcctId The checking account to open from
     * @param initialDeposit the initial deposit into the savings account
     * @return true or false based on success
     * @throws SQLException ensure proper sql connection
     */
    public boolean openSavingsFromChecking(int userId,
                                           int checkingAcctId,
                                           double initialDeposit) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            AccountDAO acctDao = new AccountDAO();
            TransactionDAO txDao = new TransactionDAO();

            //Create a savings with 0 balance
            Account.SavingsAccount sav =
                    new Account.SavingsAccount(userId, 0.0, DEFAULT_SAVINGS_RATE);
            if (!acctDao.createAccount(sav, conn)) { conn.rollback(); return false; }

            //Transfer funds from checking to savings
            if (!acctDao.withdraw(conn, checkingAcctId, initialDeposit)) { conn.rollback(); return false; }
            if (!acctDao.deposit (conn, sav.getACCOUNT_NUMBER(), initialDeposit)) { conn.rollback(); return false; }

            // log both of the transactions
            txDao.logTransaction(conn, checkingAcctId,          // FROM
                    "WITHDRAWAL", initialDeposit,
                    "Open savings", "COMPLETED");

            txDao.logTransaction(conn, sav.getACCOUNT_NUMBER(), // TO
                    "DEPOSIT",    initialDeposit,
                    "Initial funding", "COMPLETED");

            conn.commit();
            System.out.println("✓ Savings account ID " + sav.getACCOUNT_NUMBER()
                    + " opened with $" + initialDeposit);
            return true;
        }
    }


    /**
     * Open a default checking account with 0 balance
     * @param userId The user id to make the account for
     * @param connection the sql connection
     * @throws SQLException ensure proper connection
     */
    public void openDefaultChecking(int userId, Connection connection) throws SQLException {
        Account.CheckingAccount checkingAccount = new Account.CheckingAccount(userId, 0.00);
        new AccountDAO().createAccount(checkingAccount, connection);
        System.out.println("  → Created checking account ID: " + checkingAccount.getACCOUNT_NUMBER());
    }
}
