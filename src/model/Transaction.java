package model;

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
     * The transaction posted
     * @param transactionNumber The transaction number associated
     * @param accountNumber The account number of the account making the transaction
     * @param transcationType the type of transaction
     * @param amount the amount the transaction was for
     * @param timeStamp the timestamp of the transaction
     * @param description a short description
     * @param status the status of the transaction
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

    public static void PostTransaction(int transactionNumber, int accountNumber, String transcationType,
                                       double amount, Time timeStamp, String description, String status){ }

    /* ------------ getter methods required by RegistrationUI ------------ */
    public int    getTRANSACTION_NUMBER() { return TRANSACTION_NUMBER; }
    public String getTRANSCATION_TYPE()   { return TRANSCATION_TYPE; }
    public double getAMOUNT()             { return AMOUNT; }
    public Time   getTIME_STAMP()         { return TIME_STAMP; }
    public String getDESCRIPTION()        { return DESCRIPTION; }
    public String getStatus()             { return Status; }
    public int    getACCOUNT_NUMBER()     { return ACCOUNT_NUMBER; }
}
