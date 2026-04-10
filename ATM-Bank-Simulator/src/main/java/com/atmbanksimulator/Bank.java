package com.atmbanksimulator;

import java.io.Serializable;

public class Bank implements Serializable {

    public enum AccountType {
        STANDARD, STUDENT, PRIME, SAVING
    }

    private int maxAccounts = 10;
    private int numAccounts = 0;
    private BankAccount[] accounts = new BankAccount[maxAccounts];
    private BankAccount loggedInAccount = null;

    public BankAccount makeBankAccount(String accNumber, String accPasswd, int balance) {
        return new BankAccount(accNumber, accPasswd, balance);
    }

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

    public boolean addBankAccount(BankAccount a) {
        if (numAccounts < maxAccounts) {
            accounts[numAccounts] = a;
            numAccounts++;
            return true;
        } else {
            return false;
        }
    }

    public boolean addBankAccount(String accNumber, String accPasswd, int balance) {
        return addBankAccount(makeBankAccount(accNumber, accPasswd, balance));
    }

    public boolean addBankAccount(String accNumber, String accPasswd, int balance, AccountType type) {
        return addBankAccount(makeBankAccount(accNumber, accPasswd, balance, type));
    }

    public boolean login(String accountNumber, String password) {
        logout();

        for (BankAccount b : accounts) {
            if (b != null && b.getAccNumber().equals(accountNumber) && b.getaccPasswd().equals(password)) {
                loggedInAccount = b;
                return true;
            }
        }
        loggedInAccount = null;
        return false;
    }

    public void logout() {
        if (loggedIn()) {
            loggedInAccount = null;
        }
    }

    public boolean loggedIn() {
        return loggedInAccount != null;
    }

    public boolean deposit(int amount) {
        if (loggedIn()) {
            return loggedInAccount.deposit(amount);
        } else {
            return false;
        }
    }

    public boolean withdraw(int amount) {
        if (loggedIn()) {
            return loggedInAccount.withdraw(amount);
        } else {
            return false;
        }
    }

    public int getBalance() {
        if (loggedIn()) {
            return loggedInAccount.getBalance();
        } else {
            return -1;
        }
    }

    public int getNumAccounts() {
        return numAccounts;
    }

    public boolean transfer(String targetAccountNumber, int amount) {
        if (!loggedIn()) {
            return false;
        }

        BankAccount target = null;

        for (BankAccount acc : accounts) {
            if (acc != null && acc.getAccNumber().equals(targetAccountNumber)) {
                target = acc;
                break;
            }
        }

        if (target == null) {
            return false;
        }

        if (loggedInAccount.withdraw(amount)) {
            target.deposit(amount);
            return true;
        }

        return false;
    }

    public boolean changePassword(String old, String newP) {
        if (loggedIn()) {
            return loggedInAccount.changePassword(old, newP);
        }
        return false;
    }

    // ===== NEW METHOD FOR MINI STATEMENT =====
    public BankAccount getLoggedInAccount() {
        return loggedInAccount;
    }
}