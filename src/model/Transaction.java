package model;//Author: Ricky Franco
//25 Mar 2025
//model.Transaction.java:

import java.sql.Time;

public class Transaction {

    private final int TRANSACTION_NUMBER;
    private final int ACCOUNT_NUMBER;
    private final String TRANSCATION_TYPE;
    private final double AMOUNT;
    private final Time TIME_STAMP;
    private final String DESCRIPTION;
    private String Status;

    /**
     * A constructor for a transaction
     * @param transactionNumber the number of the transaction
     * @param accountNumber the account number linked to the transaction
     * @param transcationType the type of transaction
     * @param amount the amount of money in the transaction
     * @param timeStamp the current time when made
     * @param description optional description
     * @param status success of the transaction
     */
    public Transaction(int transactionNumber, int accountNumber, String transcationType,
                       double amount, Time timeStamp, String description, String status) {
        this.TRANSACTION_NUMBER = transactionNumber;
        this.ACCOUNT_NUMBER     = accountNumber;
        this.TRANSCATION_TYPE   = transcationType;
        this.AMOUNT             = amount;
        this.TIME_STAMP         = timeStamp;
        this.DESCRIPTION        = description;
        this.Status             = status;
    }

}
