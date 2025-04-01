package DAO;

import model.Transaction;

import java.sql.Connection;
import java.util.ArrayList;

public class TransactionDAO {

    /**
     * @param connection SQL connection
     * @param accountID AccountID associated with the transaction
     * @param transactionType type of transaction (withdraw, deposit)
     * @param amount dollar amount of the transaction
     * @param description short summary of transaction
     * @return true or false based on success
     */
    public boolean logTransaction(Connection connection, int accountID, String transactionType, double amount, String description){
        //Attempt to post transaction
        if(true){
            return true;

        }
        return false;
    }

    /**
     * @param accountID AccountId associated with the transatcions
     * @param connection SQL connection
     * @return a list of transactions
     */
    public ArrayList<Transaction> getTransactionsByAccount(int accountID, Connection connection){

        //Attempt to grab transactions for a specific account
        if(true){
            return null ;
        }
        return null;
    }
}
