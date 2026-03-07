package com.atmbanksimulator;

/*
 * Author: Daniella
 *
 * SavingAccount represents a savings account that earns interest.
 *
 * Special Feature:
 * - Interest can be applied to the account balance.
 *
 * Example:
 * Balance = £1000
 * InterestRate = 0.02 (2%)
 *
 * Interest = £20
 * New Balance = £1020
 */

public class SavingAccount extends BankAccount {

    // Interest rate for savings account
    // 0.02 = 2%
    private double interestRate = 0.02;

    /*
     * Constructor
     *
     * @param accNumber account number
     * @param accPasswd account password
     * @param balance starting balance
     */
    public SavingAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    /*
     * Adds interest to the account balance.
     *
     * Formula:
     * interest = balance * interestRate
     *
     * We deposit the interest back into the account.
     */
    public void addInterest() {

        int currentBalance = getBalance();

        int interest = (int) (currentBalance * interestRate);

        // Deposit interest into account
        deposit(interest);
    }

    /*
     * Getter for interest rate
     */
    public double getInterestRate() {
        return interestRate;
    }
}
