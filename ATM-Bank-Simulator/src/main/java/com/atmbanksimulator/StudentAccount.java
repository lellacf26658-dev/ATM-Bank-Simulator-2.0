package com.atmbanksimulator;

// StudentAccount (Domain / Service / Business Logic)
// Author: Reema
//
// StudentAccount extends BankAccount with a daily withdrawal limit.
// Special Feature:
//   - withdrawnToday tracks cumulative withdrawals in the current session.
//   - If withdrawnToday + amount would exceed dailyLimit, the withdrawal is rejected.
//
// CHANGES FROM ORIGINAL:
// 1. [NEW] getAccountTypeName() — overrides BankAccount to return "STUDENT"
//    so the View can display the correct account type badge.
//
// 2. [NEW] getDailyLimitInfo() — returns a formatted string showing the student
//    how much of their daily limit they have used and how much remains.
//    UIModel displays this after each withdrawal on a StudentAccount.
//
// All original logic (dailyLimit, withdrawnToday, withdraw, getRemainingLimit,
// getWithdrawnToday, resetDailyLimit) is preserved exactly.

public class StudentAccount extends BankAccount {

    // Maximum amount the student can withdraw in a single day
    private int dailyLimit = 50;

    // Running total of how much has been withdrawn today (resets each session)
    private int withdrawnToday = 0;


    // Constructor — calls parent BankAccount constructor (unchanged)
    public StudentAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }


    // withdraw() — Overridden from BankAccount
    // [UNCHANGED from original]
    // Checks if today's total withdrawals plus the new amount would
    // exceed the dailyLimit. If so, rejects the withdrawal.
    // On success, increments withdrawnToday.
    @Override
    public boolean withdraw(int amount) {
        // Check daily limit — reject if it would be exceeded
        if (withdrawnToday + amount > dailyLimit) {
            return false;
        }

        // Attempt the normal withdrawal via parent class
        boolean success = super.withdraw(amount);

        // Only update the daily tracker if the withdrawal actually succeeded
        if (success) {
            withdrawnToday += amount;
        }

        return success;
    }


    // [NEW] getAccountTypeName()
    // Overrides BankAccount's version to return "STUDENT".
    @Override
    public String getAccountTypeName() {
        return "STUDENT";
    }


    // getRemainingLimit() — Returns how much more can be withdrawn today
    // [UNCHANGED from original]
    public int getRemainingLimit() {
        return dailyLimit - withdrawnToday;
    }


    // getWithdrawnToday() — Returns total withdrawn in current session
    // [UNCHANGED from original]
    public int getWithdrawnToday() {
        return withdrawnToday;
    }


    // resetDailyLimit() — Resets the daily withdrawal counter to 0
    // [UNCHANGED from original]
    // Would be called at midnight in a real banking system.
    public void resetDailyLimit() {
        withdrawnToday = 0;
    }


    // [NEW] getDailyLimitInfo()
    // Returns a formatted string showing the student their daily limit status.
    // UIModel displays this after a StudentAccount withdrawal so the student
    // always knows their remaining allowance.
    public String getDailyLimitInfo() {
        return String.format(
                "Daily Limit:    £%d\n" +
                        "Withdrawn:      £%d\n" +
                        "Remaining:      £%d",
                dailyLimit, withdrawnToday, getRemainingLimit()
        );
    }


    // getDailyLimit() — Getter for the daily limit value (new, for display)
    public int getDailyLimit() {
        return dailyLimit;
    }
}
