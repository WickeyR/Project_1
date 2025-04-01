package model;
//Author: Ricky Franco
//25 Mar 2025
//model.SavingsAccount.java:

public class SavingsAccount extends Account {

    private double interestRate;

    /**
     * @param acctNumber the accounts unique identifier
     * @param userid the users' unique idintifier
    \     * @param dateOpened The account opening date
     * @param initialBalance The initial balance
     * @param interestRate The interest rate to be applied to the balance
     */
    public SavingsAccount(int acctNumber, int userid, String dateOpened, int initialBalance, double interestRate) {
        super(acctNumber, userid, "Savings", dateOpened, initialBalance);
        this.interestRate = interestRate;

    }

    public SavingsAccount(int acctNumber, int userid, String dateOpened, double interestRate) {
        super(acctNumber, userid, "Savings", dateOpened);
        this.interestRate = interestRate;

    }


    //------------------ GETTER METHODS --------------//

    /**
     * @param newInterestRate The new interest rate for the account
     * Allows a new interest rate to be set
     */
    public void setInterestRate(double newInterestRate){
        this.interestRate = newInterestRate;
    }

    //------------------ SETTER METHODS --------------//

    /**
     * @return Returns the interest rate
     */
    public double getInterestRate(){
        return this.interestRate;
    }


    /**
     * calculate the interest to be added to the account
     */
    public void calculateInterest(){
        balance += (balance * interestRate);
    }
}

