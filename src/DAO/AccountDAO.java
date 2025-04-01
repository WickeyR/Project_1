package DAO;

import java.sql.Connection;
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
    public boolean createAccount(Account account, Connection connection){

        //Attempt to create a new account through sql connection
        if(true){
            return true;
        }

        return false;
    }


    /**
     * @param accountID Users account id
     * @param newBalance users updated balance
     * @param connection SQL connection
     * @return true or false based on success
     */
    public boolean updateAccountBalance(int accountID, double newBalance, Connection connection){

        //Attempt to update balance
        if(true){
            return true;
        }
        return false;
    }

    /**
     * @param connection SQL connnection
     * @param accountID users account ID
     * @param amount amount to withdraw
     * @return true or false based on success
     */
    public boolean withdraw(Connection connection, int accountID,double amount){

        //Attempt to withdraw
        if(true){
            return true;
        }
        return false;
    }

    /**
     * @param connection SQL connection
     * @param accountID users account ID
     * @param amount amount ot deposi t
     * @return true or false based on success
     */
    public boolean deposit(Connection connection, int accountID,double amount){

        //Attempt to deposit
        if(true){
            return true;
        }
        return false;
    }

}
