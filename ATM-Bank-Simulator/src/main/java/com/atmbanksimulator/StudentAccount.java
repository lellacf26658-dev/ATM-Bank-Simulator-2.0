package com.atmbanksimulator;

public class StudentAccount extends BankAccount {

    private int dailyLimit = 50;
    private int withdrawnToday = 0;

    public StudentAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    @Override
    public boolean withdraw(int amount) {
        if (withdrawnToday + amount > dailyLimit) {
            return false;
        }

        boolean success = super.withdraw(amount);

        if (success) {
            withdrawnToday += amount;
        }

        return success;
    }

    public int getRemainingLimit() {
        return dailyLimit - withdrawnToday;
    }

    public int getWithdrawnToday() {
        return withdrawnToday;
    }

    public void resetDailyLimit() {
        withdrawnToday = 0;
    }
}