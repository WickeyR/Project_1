package model;//Author: Ricky Franco
//25 Mar 2025
//model.Account.java:

public abstract class Account {

    private final int ACCOUNT_NUMBER;
    private final int USER_ID;

    private final String ACCOUNT_TYPE;

    private final String DATE_OPENED;

    protected double balance;

    private String status;

    /**
     * @param acctNumber the accounts unique identifier
     * @param userid the users' unique idintifier
     * @param accountType The account type (Checkings, Savings)
     * @param dateOpened The account opening date
     * @param initialBalance The initial balance
     */
    public Account(int acctNumber, int userid, String accountType, String dateOpened, int initialBalance){
        this.ACCOUNT_NUMBER = acctNumber;
        this.USER_ID = userid;
        this.ACCOUNT_TYPE = accountType;
        this.DATE_OPENED = dateOpened;
        this.balance = initialBalance;
    }
    public Account(int acctNumber, int userid, String accountType, String dateOpened){
        this.ACCOUNT_NUMBER = acctNumber;
        this.USER_ID = userid;
        this.ACCOUNT_TYPE = accountType;
        this.DATE_OPENED = dateOpened;
        this.balance = 0;
    }



    //------------------ GETTER METHODS --------------//

    /**
     * @return model.Account number
     */
    public int getACCOUNT_NUMBER() {
        return ACCOUNT_NUMBER;
    }

    /**
     * @return users' unique ID
     */
    public int getUSER_ID(){
        return USER_ID;
    }

    /**
     * @return type of account
     */
    public String getACCOUNT_TYPE(){
        return ACCOUNT_TYPE;
    }

    /**
     * @return date of account opening
     */
    public String getDATE_OPENED(){
        return DATE_OPENED;
    }

    /**
     * @return account balance
     */
    public double getBalance(){
        return balance;
    }

    /**
     * @return status of account
     */
    public String getStatus(){
        return status;
    }


    //------------------ SETTER METHODS --------------//


    /**
     * @param newStatus sets the new status of the account
     */
    public void setStatus(String newStatus){
        this.status = newStatus;
    }

    //------------------ OTHER METHODS --------------//

    /**
     * @param amount the amount of money to deposit into the account
     * @return true or false based on success
     */
    public boolean deposit(double amount){
        if(amount > 0){
            this.balance += amount;
            return true;
        }return false;
    }

    /**
     * @param amount the amount of money to withdraw from the account
     * @return true or false based on success
     */
    public boolean withdraw(double amount){

        if(((balance - amount) > 0) && amount > 0){
            balance -= amount;
            return true;
        }
        return false;
    }
}

