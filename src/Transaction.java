//Author: Ricky Franco
//25 Mar 2025
//Transaction.java:

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
     * @param transactionNumber
     * @param accountNumber
     * @param transcationType
     * @param amount
     * @param timeStamp
     * @param description
     * @param status
     */
    public Transaction(int transactionNumber, int accountNumber, String transcationType, double amount, Time timeStamp, String description, String status) {
        this.TRANSACTION_NUMBER = transactionNumber;
        ACCOUNT_NUMBER = accountNumber;
        TRANSCATION_TYPE = transcationType;
        AMOUNT = amount;
        TIME_STAMP = timeStamp;
        DESCRIPTION = description;
        Status = status;


        PostTransaction(transactionNumber, accountNumber,transcationType, amount, timeStamp, description, status );

    }

    /**
     * @param transactionNumber The transaction number
     * @param accountNumber the unique account number
     * @param transcationType the type of transaction (deposit, withdrawal, transfer)
     * @param amount the transaction amount
     * @param timeStamp the time at which the transaction occured
     * @param description the purpose of the transaction
     * @param status the status (completed, failed, pending)
     * post the transaction to the sql database
     */
    public static void PostTransaction(int transactionNumber, int accountNumber, String transcationType, double amount, Time timeStamp, String description, String status){
    }
}

