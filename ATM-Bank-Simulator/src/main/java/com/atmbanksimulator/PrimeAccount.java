package com.atmbanksimulator;

// PrimeAccount (Domain / Service / Business Logic)
// Author: Daniella
//
// PrimeAccount represents a premium bank account.
// Special Feature:
//   - Allows users to withdraw money even if their balance is low.
//   - The account CAN go into overdraft up to a specified limit.
//
// Example:
//   Balance = £50, OverdraftLimit = £100
//   Maximum withdrawal allowed = £150 (balance + overdraft)
//
// CHANGES FROM ORIGINAL:
// 1. [BUG FIX] handleOverdraft() — The original code calculated 'deficit'
//    but NEVER APPLIED IT. The balance never actually became negative.
//    Fix: Use setBalance(currentBalance - amount) to correctly set a
//    negative balance within the overdraft range.
//    See detailed comment in handleOverdraft() below.
//
// 2. [NEW] getAccountTypeName() — overrides BankAccount to return "PRIME"
//    so the View can display the correct account type badge.
//
// 3. [NEW] getOverdraftRemaining() — shows how much overdraft is still available,
//    useful for the UIModel to display to the user after a withdrawal.
//
// This class EXTENDS BankAccount and overrides the withdraw() method
// to allow overdraft behaviour.

public class PrimeAccount extends BankAccount {

    // Maximum overdraft allowed for this account (in pounds)
    private int overdraftLimit = 100;


    // Constructor
    // Calls the parent BankAccount constructor to set accNumber, accPasswd, balance.
    // @param accNumber  account number
    // @param accPasswd  account password
    // @param balance    starting balance
    public PrimeAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }


    // withdraw() — Overridden from BankAccount
    //
    // Normal BankAccount: cannot withdraw more than balance.
    // PrimeAccount:       can withdraw up to balance + overdraftLimit.
    //
    // Example: balance = £50, overdraftLimit = £100
    //   withdraw(£120) → allowed (50 + 100 = 150 ≥ 120)
    //   withdraw(£160) → rejected (exceeds 150)
    //   After withdraw(£120) → balance = -£70
    @Override
    public boolean withdraw(int amount) {
        // Reject negative withdrawal amounts immediately
        if (amount < 0) {
            return false;
        }

        // Calculate the total funds available including overdraft headroom
        // e.g. balance = £50, overdraftLimit = £100 → availableFunds = £150
        int availableFunds = getBalance() + overdraftLimit;

        if (amount > availableFunds) {
            // Withdrawal exceeds the overdraft allowance — refuse it
            return false;
        }

        // Attempt a standard withdrawal first (works if amount ≤ balance)
        // If that succeeds, we're done — no overdraft needed.
        // If it fails (balance < amount), fall through to handleOverdraft.
        return super.withdraw(amount) || handleOverdraft(amount);
    }


    // handleOverdraft()
    // Called when the normal withdraw() fails (i.e. amount > current balance).
    // This method correctly sets the balance to a negative value within
    // the allowed overdraft range.
    //
    // [BUG FIX EXPLANATION]
    // ORIGINAL CODE:
    //   int deficit = amount - currentBalance;    // ← calculated but NEVER USED
    //   if (currentBalance > 0) {
    //       super.withdraw(currentBalance);        // ← drains balance to 0
    //   }
    //   return true;                               // ← balance stays at 0, NOT negative!
    //
    // The bug: the 'deficit' variable was computed but the remaining amount
    // was never subtracted. The account always ended at £0 instead of going
    // into a proper negative balance.
    //
    // FIX: Use setBalance() (inherited from BankAccount, protected access)
    //   to directly set balance = currentBalance - amount.
    //   This correctly produces a negative balance for overdraft scenarios.
    //   Then record the transaction with the correct overdraft type.
    //
    // @param amount — the withdrawal amount that the normal withdraw() rejected
    // @return true if overdraft withdrawal is within the allowed limit
    private boolean handleOverdraft(int amount) {
        int currentBalance = getBalance();

        // Double-check the overdraft range (should always be true here,
        // but guarding against edge cases)
        if (currentBalance - amount >= -overdraftLimit) {

            // [FIX] Directly set the balance to the correct negative value.
            // setBalance() is the protected setter in BankAccount that lets
            // subclasses update the balance when normal deposit/withdraw won't do.
            setBalance(currentBalance - amount); // e.g. 50 - 120 = -70 ✓

            // Record this as a WITHDRAWAL transaction (not an overdraft fee,
            // as fees are a future enhancement — see Daniella's Week 5+ task)
            recordTransaction(Transaction.TransactionType.WITHDRAWAL, amount);

            return true;
        }

        return false; // Should not reach here given the check in withdraw()
    }


    // [NEW] getAccountTypeName()
    // Overrides BankAccount's version to return "PRIME".
    // The View uses this to show the account type badge on screen.
    @Override
    public String getAccountTypeName() {
        return "PRIME";
    }


    // getOverdraftLimit() — Getter for overdraft limit
    // Useful for displaying account details in UIModel/View.
    // (unchanged from original)
    public int getOverdraftLimit() {
        return overdraftLimit;
    }


    // [NEW] getOverdraftRemaining()
    // Returns how much overdraft headroom is still available.
    // e.g. balance = -£30, overdraftLimit = £100 → remaining = £70
    // UIModel uses this to inform the user after a withdrawal.
    public int getOverdraftRemaining() {
        return overdraftLimit + getBalance(); // balance may be negative
    }
}
