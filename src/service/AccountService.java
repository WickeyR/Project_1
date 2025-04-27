package service;

import DAO.AccountDAO;
import model.Account;

import java.sql.Connection;
import java.sql.SQLException;

public class AccountService {



    public void openDefaultChecking(int userId, Connection connection) throws SQLException {
        Account.CheckingAccount checkingAccount = new Account.CheckingAccount(userId, 0.00);
        new AccountDAO().createAccount(checkingAccount, connection);
        System.out.println("  → Created checking account ID: " + checkingAccount.getACCOUNT_NUMBER());
    }
}
