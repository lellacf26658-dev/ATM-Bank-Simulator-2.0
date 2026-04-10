package com.atmbanksimulator;

import java.io.Serializable;
import java.util.ArrayList;

public class BankAccount implements Serializable {
    private String accNumber = "";
    private String accPasswd = "";
    protected int balance = 0;
    private ArrayList<Transaction> transactions = new ArrayList<>();

    public BankAccount() {
    }

    public BankAccount(String a, String p, int b) {
        accNumber = a;
        accPasswd = p;
        balance = b;
    }

    public void addTransaction(String type, int amount) {
        transactions.add(new Transaction(type, amount));
    }

    public String getMiniStatement() {
        StringBuilder sb = new StringBuilder("=== Last 5 Transactions ===\n");
        int start = Math.max(0, transactions.size() - 5);
        for (int i = start; i < transactions.size(); i++) {
            sb.append(transactions.get(i)).append("\n");
        }
        return sb.toString();
    }

    public boolean withdraw(int amount) {
        if (amount < 0 || balance < amount) {
            return false;
        } else {
            balance = balance - amount;
            addTransaction("WITHDRAW", amount);
            return true;
        }
    }

    public boolean deposit(int amount) {
        if (amount < 0) {
            return false;
        } else {
            balance = balance + amount;
            addTransaction("DEPOSIT", amount);
            return true;
        }
    }

    public int getBalance() {
        return balance;
    }

    public String getAccNumber() {
        return accNumber;
    }

    public String getaccPasswd() {
        return accPasswd;
    }

    protected void setBalance(int newBalance) {
        this.balance = newBalance;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (this.accPasswd.equals(oldPassword)) {
            if (newPassword != null && newPassword.length() >= 5) {
                this.accPasswd = newPassword;
                return true;
            }
        }
        return false;
    }
}