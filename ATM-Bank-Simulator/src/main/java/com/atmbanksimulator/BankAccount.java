package com.atmbanksimulator;

// ===== 📚🌐BankAccount (Domain / Service / Business Logic) =====

// BankAccount class:
// - Stores instance variables for account number, password, and balance
// - Provides methods to withdraw, deposit, check balance, etc.
public class BankAccount {
    private String accNumber = "";
    private String accPasswd = "";
    //private int balance = 0; - commented out
    //private → only this class (BankAcc) can access it
    //protected → this class and subclasses (PrimeAcc, SavingAcc) can access it
    // Daniella - changed ^private to:
    protected int balance = 0;

    public BankAccount() {
    }

    public BankAccount(String a, String p, int b) {
        accNumber = a;
        accPasswd = p;
        balance = b;
    }

    // Withdraw money from this account.
    // Returns true if successful, or false if the amount is negative or exceeds the current balance.
    public boolean withdraw(int amount) {
        if (amount < 0 || balance < amount) {
            return false;
        } else {
            balance = balance - amount;  // subtract amount from balance
            return true;
        }
    }

    // deposit the amount of money into this account.
    // Return true if successful,or false if the amount is negative
    public boolean deposit(int amount) {
        if (amount < 0) {
            return false;
        } else {
            balance = balance + amount;  // add amount to balance
            return true;
        }
    }

    // can this pls work
    // Getter for the account balance
    // Returns the current balance of this account
    public int getBalance() {
        return balance;
    }

    // Getter for the account number
    public String getAccNumber() {
        return accNumber;
    }

    // Getter for the account password
    public String getaccPasswd() {
        return accPasswd;
    }

    /*
     * Protected method allowing subclasses
     * (PrimeAccount and SavingAccount)
     * to update balance safely.
     */
    protected void setBalance(int newBalance) {
        this.balance = newBalance;
    }

    // Haaroun: Logic to change the password
    public boolean changePassword(String oldPassword, String newPassword) {
        // 1. Check if the old password provided matches the one we have stored
        if (this.accPasswd.equals(oldPassword)) {

            // 2. Validation: Ensure the new password isn't empty and is at least 5 characters
            if (newPassword != null && newPassword.length() >= 5) {
                this.accPasswd = newPassword; // Update to the new password
                return true; // Success!
            }
        }
        // If the old password was wrong OR the new one was too short
        return false;
    }
}