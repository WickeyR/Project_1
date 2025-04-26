package model;

import java.time.LocalDate;

/**
 * Abstract base class for bank accounts.
 */
public abstract class Account {
    private final int ACCOUNT_NUMBER;
    private final int USER_ID;
    private final String ACCOUNT_TYPE;
    private final LocalDate DATE_OPENED;
    protected double balance;
    private String status;

    /**
     * Constructs a new account with an initial balance.
     * @param acctNumber the account's unique identifier
     * @param userId the user's unique identifier
     * @param accountType the type of account ("CHECKING", "SAVINGS")
     * @param initialBalance the starting balance
     */
    public Account(int acctNumber, int userId, String accountType, double initialBalance) {
        this.ACCOUNT_NUMBER = acctNumber;
        this.USER_ID = userId;
        this.ACCOUNT_TYPE = accountType;
        this.DATE_OPENED = LocalDate.now();
        this.balance = initialBalance;
        this.status = "ACTIVE";
    }

    //------------------ GETTERS ------------------//
    public int getAccountNumber() {
        return ACCOUNT_NUMBER;
    }
    public int getUserId() {
        return USER_ID;
    }
    public String getAccountType() {
        return ACCOUNT_TYPE;
    }
    public LocalDate getDateOpened() {
        return DATE_OPENED;
    }
    public double getBalance() {
        return balance;
    }
    public String getStatus() {
        return status;
    }

    //------------------ SETTERS ------------------//
    public void setStatus(String status) {
        this.status = status;
    }

    //------------------ OPERATIONS ------------------//
    /**
     * Deposit funds into the account.
     * @param amount the amount to deposit
     * @return true if deposit successful; false otherwise
     */
    public boolean deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            return true;
        }
        return false;
    }

    /**
     * Withdraw funds from the account.
     * @param amount the amount to withdraw
     * @return true if withdrawal successful; false otherwise
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
        public CheckingAccount(int acctNumber, int userId, double initialBalance) {
            super(acctNumber, userId, "CHECKING", initialBalance);
        }
    }

    /**
     * Savings account: has an interest rate.
     */
    public static class SavingsAccount extends Account {
        private double interestRate;

        /**
         * @param acctNumber the account's unique identifier
         * @param userId the user's unique identifier
         * @param initialBalance the starting balance
         * @param interestRate the annual interest rate (e.g., 0.02 for 2%)
         */
        public SavingsAccount(int acctNumber, int userId, double initialBalance, double interestRate) {
            super(acctNumber, userId, "SAVINGS", initialBalance);
            this.interestRate = interestRate;
        }

        public double getInterestRate() {
            return interestRate;
        }

        public void setInterestRate(double interestRate) {
            this.interestRate = interestRate;
        }

        /**
         * Apply interest to the balance.
         */
        public void applyInterest() {
            if (interestRate > 0) {
                balance += balance * interestRate;
            }
        }
    }
}
