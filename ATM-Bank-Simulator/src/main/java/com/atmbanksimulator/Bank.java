package com.atmbanksimulator;

// ===== Bank (Domain / Service / Business Logic) =====

// Bank class: a simple implementation of a bank, containing a list of bank accounts
// and has a currently logged-in account (loggedInAccount).
public class Bank {

    // Enum representing different types of bank accounts
    public enum AccountType {
        STANDARD, STUDENT, PRIME, SAVING
    }

    // ToDO: Optional extension:
    // Improve account management in the Bank class:
    // Replace Array with ArrayList for managing BankAccount objects.
    // Refactor addBankAccount and login methods to leverage ArrayList.

    // Instance variables storing bank information
    private int maxAccounts = 10;                       // Maximum number of accounts the bank can hold
    private int numAccounts = 0;                        // Current number of accounts in the bank
    private BankAccount[] accounts = new BankAccount[maxAccounts];  // Array to hold BankAccount objects
    private BankAccount loggedInAccount = null;         // Currently logged-in account ('null' if no one is logged in)

    // Factory method to create a new BankAccount
    public BankAccount makeBankAccount(String accNumber, String accPasswd, int balance) {
        return new BankAccount(accNumber, accPasswd, balance);
    }

    // Factory method to create a BankAccount based on account type
    public BankAccount makeBankAccount(String accNumber, String accPasswd, int balance, AccountType type) {
        switch (type) {
            case STUDENT:
                return new StudentAccount(accNumber, accPasswd, balance);
            case PRIME:
                return new PrimeAccount(accNumber, accPasswd, balance);
            case SAVING:
                return new SavingAccount(accNumber, accPasswd, balance);
            default:
                return new BankAccount(accNumber, accPasswd, balance);
        }
    }

    // Add a BankAccount object to the bank
    public boolean addBankAccount(BankAccount a) {
        if (numAccounts < maxAccounts) {
            accounts[numAccounts] = a;
            numAccounts++ ;
            return true;
        } else {
            return false;
        }
    }

    // Create and add a BankAccount using account number, password, and balance
    public boolean addBankAccount(String accNumber, String accPasswd, int balance) {
        return addBankAccount(makeBankAccount(accNumber, accPasswd, balance));
    }

    // Create and add a BankAccount with a specific account type
    public boolean addBankAccount(String accNumber, String accPasswd, int balance, AccountType type) {
        return addBankAccount(makeBankAccount(accNumber, accPasswd, balance, type));
    }

    // Attempt to log in with the given account number and password
    public boolean login(String accountNumber, String password) {
        logout(); // logout of any previous loggedInAccount

        // Search the accounts array for a matching account number and password
        for (BankAccount b: accounts) {
            if (b != null && b.getAccNumber().equals(accountNumber) && b.getaccPasswd().equals(password)) {
                loggedInAccount = b;
                return true;
            }
        }
        loggedInAccount = null;
        return false;
    }

    // Log out the currently logged-in account
    public void logout() {
        if (loggedIn()) {
            loggedInAccount = null;
        }
    }

    // Check if there is a logged-in account
    public boolean loggedIn() {
        return loggedInAccount != null;
    }

    // Deposit amount into the logged-in account
    public boolean deposit(int amount) {
        if (loggedIn()) {
            return loggedInAccount.deposit(amount);
        } else {
            return false;
        }
    }

    // Withdraw amount from the logged-in account
    public boolean withdraw(int amount) {
        if (loggedIn()) {
            return loggedInAccount.withdraw(amount);
        } else {
            return false;
        }
    }

    // Get the balance of the logged-in account
    public int getBalance() {
        if (loggedIn()) {
            return loggedInAccount.getBalance();
        } else {
            return -1; // Error indicator
        }
    }

    /*
     * Author: Daniella
     *
     * Transfer money from the currently logged-in account
     * to another account in the bank.
     */
    public boolean transfer(String targetAccountNumber, int amount) {

        // Must be logged in
        if (!loggedIn()) {
            return false;
        }

        // Find target account
        BankAccount target = null;

        for (BankAccount acc : accounts) {

            if (acc != null && acc.getAccNumber().equals(targetAccountNumber)) {
                target = acc;
                break;
            }
        }

        // Target account does not exist
        if (target == null) {
            return false;
        }

        // Attempt withdrawal from current account
        if (loggedInAccount.withdraw(amount)) {

            // Deposit into target account
            target.deposit(amount);

            return true;
        }

        return false;
    }
}