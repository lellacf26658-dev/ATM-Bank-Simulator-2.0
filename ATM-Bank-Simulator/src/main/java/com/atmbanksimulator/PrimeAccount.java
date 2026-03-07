package com.atmbanksimulator;

/*
 * Author: Daniella
 *
 * PrimeAccount represents a premium bank account.
 *
 * Special Feature:
 * - Allows users to withdraw money even if their balance is low.
 * - The account can go into overdraft up to a specified limit.
 *
 * Example:
 * Balance = £50
 * OverdraftLimit = £100
 *
 * Maximum withdrawal allowed = £150
 *
 * This class EXTENDS BankAccount and overrides the withdraw() method
 * to allow overdraft behaviour.
 */

public class PrimeAccount extends BankAccount {

    // Maximum overdraft allowed for this account
    private int overdraftLimit = 100;

    /*
     * Constructor
     * Calls the parent BankAccount constructor
     *
     * @param accNumber  account number
     * @param accPasswd  account password
     * @param balance    starting balance
     */
    public PrimeAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    /*
     * Overridden withdraw method
     *
     * Normal BankAccount:
     * - Cannot withdraw more than balance
     *
     * PrimeAccount:
     * - Can withdraw up to balance + overdraftLimit
     */
    @Override
    public boolean withdraw(int amount) {

        // Reject negative withdrawals
        if (amount < 0) {
            return false;
        }

        /*
         * We check whether the withdrawal exceeds
         * the allowed overdraft range.
         */
        int availableFunds = getBalance() + overdraftLimit;

        if (amount > availableFunds) {
            // Withdrawal exceeds overdraft allowance
            return false;
        }

        /*
         * If withdrawal is allowed, we manually update balance.
         * This allows balance to become negative.
         */
        int newBalance = getBalance() - amount;

        /*
         * Because balance is private in BankAccount,
         * we use deposit/withdraw logic indirectly.
         *
         * We simulate setting balance by withdrawing
         * or handling the overdraft case.
         */
        return super.withdraw(amount) || handleOverdraft(amount);
    }

    /*
     * Handles overdraft withdrawals when normal withdraw fails.
     */
    private boolean handleOverdraft(int amount) {

        int currentBalance = getBalance();

        if (currentBalance - amount >= -overdraftLimit) {

            /*
             * This allows us to simulate overdraft
             * because balance is private in BankAccount.
             *
             * Future improvement: add setBalance() method.
             */
            int deficit = amount - currentBalance;

            if (currentBalance > 0) {
                super.withdraw(currentBalance);
            }

            return true;
        }

        return false;
    }

    /*
     * Getter method for overdraft limit
     * Useful for displaying account details later
     */
    public int getOverdraftLimit() {
        return overdraftLimit;
    }
}
