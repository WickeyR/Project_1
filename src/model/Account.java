package model;

import java.time.LocalDate;

public abstract class Account {
    /** database-assigned account ID; null until inserted */
    private Integer ACCOUNT_NUMBER;
    private final int USER_ID;
    private final String ACCOUNT_TYPE;
    private final LocalDate DATE_OPENED;
    protected double balance;
    private String status;

    /**
     * @param acctNumber    the account's unique identifier (from DB)
     * @param userId        the user's unique identifier
     * @param accountType   The account type ("CHECKING" or "SAVINGS")
     * @param dateOpened    The account opening date
     * @param initialBalance The initial balance
     */
    public Account(int acctNumber, int userId, String accountType, LocalDate dateOpened, double initialBalance) {
        this.ACCOUNT_NUMBER = acctNumber;
        this.USER_ID        = userId;
        this.ACCOUNT_TYPE   = accountType;
        this.DATE_OPENED    = dateOpened;
        this.balance        = initialBalance;
        this.status         = "ACTIVE";
    }

    /**
     * @param userId         the user's unique identifier
     * @param accountType    The account type ("CHECKING" or "SAVINGS")
     * @param initialBalance The initial balance
     *
     * Constructor for a new account; ACCOUNT_NUMBER set by DAO after insert.
     */
    public Account(int userId, String accountType, double initialBalance) {
        this.ACCOUNT_NUMBER = null;
        this.USER_ID        = userId;
        this.ACCOUNT_TYPE   = accountType;
        this.DATE_OPENED    = LocalDate.now();
        this.balance        = initialBalance;
        this.status         = "ACTIVE";
    }

    //------------------ GETTER METHODS --------------//

    /**
     * @return account number (null if not yet persisted)
     */
    public Integer getACCOUNT_NUMBER() {
        return ACCOUNT_NUMBER;
    }

    /** package-private: set by DAO after INSERT */
    public void setACCOUNT_NUMBER(int acctNumber) {
        this.ACCOUNT_NUMBER = acctNumber;
    }

    /**
     * @return users' unique ID
     */
    public int getUSER_ID() {
        return USER_ID;
    }

    /**
     * @return type of account
     */
    public String getACCOUNT_TYPE() {
        return ACCOUNT_TYPE;
    }

    /**
     * @return date of account opening
     */
    public LocalDate getDATE_OPENED() {
        return DATE_OPENED;
    }

    /**
     * @return account balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * @return status of account
     */
    public String getStatus() {
        return status;
    }

    //------------------ SETTER METHODS --------------//

    /**
     * @param newStatus sets the new status of the account
     */
    public void setStatus(String newStatus) {
        this.status = newStatus;
    }

    //------------------ OTHER METHODS --------------//

    /**
     * @param amount the amount of money to deposit into the account
     * @return true or false based on success
     */
    public boolean deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            return true;
        }
        return false;
    }

    /**
     * @param amount the amount of money to withdraw from the account
     * @return true or false based on success
     */
    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    //----------- Subclasses for Checking and Savings -----------//

    /**
     * Checking account: no additional fields.
     */
    public static class CheckingAccount extends Account {
        /**
         * @param userId         the user's unique identifier
         * @param initialBalance the starting balance
         */
        public CheckingAccount(int userId, double initialBalance) {
            super(userId, "CHECKING", initialBalance);
        }

        /**
         * @param acctNumber     the account's unique identifier (from DB)
         * @param userId         the user's unique identifier
         * @param dateOpened     the account opening date
         * @param balance        current balance
         */
        public CheckingAccount(int acctNumber, int userId, LocalDate dateOpened, double balance) {
            super(acctNumber, userId, "CHECKING", dateOpened, balance);
        }
    }

    /**
     * Savings account: has an interest rate.
     */
    public static class SavingsAccount extends Account {
        private double interestRate;

        /**
         * @param userId         the user's unique identifier
         * @param initialBalance the starting balance
         * @param interestRate   the annual interest rate (e.g., 0.02 for 2%)
         */
        public SavingsAccount(int userId, double initialBalance, double interestRate) {
            super(userId, "SAVING", initialBalance);
            this.interestRate = interestRate;
        }

        /**
         * @param acctNumber     the account's unique identifier (from DB)
         * @param userId         the user's unique identifier
         * @param dateOpened     the account opening date
         * @param balance        current balance
         * @param interestRate   the annual interest rate
         */
        public SavingsAccount(int acctNumber, int userId, LocalDate dateOpened, double balance, double interestRate) {
            super(acctNumber, userId, "SAVING", dateOpened, balance);
            this.interestRate = interestRate;
        }

        /**
         * @return the interest rate
         */
        public double getInterestRate() {
            return interestRate;
        }

        /**
         * @param newInterestRate sets a new interest rate
         */

        }
    }

