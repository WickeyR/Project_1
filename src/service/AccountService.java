package service;

import DAO.AccountDAO;
import DAO.TransactionDAO;
import model.Account;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AccountService {

    private static final double DEFAULT_SAVINGS_RATE = 0.04;   // 4 %

    /** returns true if a savings is successfully opened and funded */
    public boolean openSavingsFromChecking(int userId,
                                           int checkingAcctId,
                                           double initialDeposit) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            AccountDAO acctDao = new AccountDAO();
            TransactionDAO txDao = new TransactionDAO();

            // 1) create zero-balance savings
            Account.SavingsAccount sav =
                    new Account.SavingsAccount(userId, 0.0, DEFAULT_SAVINGS_RATE);
            if (!acctDao.createAccount(sav, conn)) { conn.rollback(); return false; }

            // 2) move funds
            if (!acctDao.withdraw(conn, checkingAcctId, initialDeposit)) { conn.rollback(); return false; }
            if (!acctDao.deposit (conn, sav.getACCOUNT_NUMBER(), initialDeposit)) { conn.rollback(); return false; }

            // 3) log both legs  (time_stamp column filled by DB)
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


    public void openDefaultChecking(int userId, Connection connection) throws SQLException {
        Account.CheckingAccount checkingAccount = new Account.CheckingAccount(userId, 0.00);
        new AccountDAO().createAccount(checkingAccount, connection);
        System.out.println("  → Created checking account ID: " + checkingAccount.getACCOUNT_NUMBER());
    }
}
