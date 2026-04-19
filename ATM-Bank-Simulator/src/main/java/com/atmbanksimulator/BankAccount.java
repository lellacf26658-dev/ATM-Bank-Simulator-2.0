package com.atmbanksimulator;

// BankAccount (Domain / Service / Business Logic)
// CHANGES FROM ORIGINAL:
// 1. [FIX]    balance changed from private to protected
// 2. [NEW]    Added ArrayList<Transaction> to record all financial operations
// 3. [MODIFY] deposit()  now records a DEPOSIT  transaction after success
// 4. [MODIFY] withdraw() now records a WITHDRAWAL transaction after success
// 5. [NEW]    recordTransaction() helper that subclasses can also call
// 6. [NEW]    getMiniStatement() returns the last 5 transactions as a formatted string
// 7. [NEW]    getAccountTypeName() returns a display label (overridden by subclasses)
// 8. [NEW]    getTransactionCount() for checking if any history exists

import java.util.ArrayList;

public class BankAccount {

    // Core account fields (unchanged from original)
    private String accNumber = "";
    private String accPasswd = "";

    // Changed this from private to protected so subclasses (PrimeAccount,SavingAccount) can read/write balance directly when needed.
    protected int balance = 0;

    // [NEW] Transaction history list.
    // Stores every financial operation performed on this account.
    // Used to display the Mini Statement (last 5 transactions).
    // ArrayList is used (not array) so the list grows dynamically.
    private ArrayList<Transaction> transactions = new ArrayList<>();

    // [NEW] Maximum transactions to keep in history (prevents memory waste
    // on long-running sessions with many operations).
    private static final int MAX_TRANSACTION_HISTORY = 50;

    // Default no-argument constructor (unchanged)
    public BankAccount() {}

    // Constructor with account number, password, and starting balance (unchanged)
    public BankAccount(String a, String p, int b) {
        accNumber = a;
        accPasswd = p;
        balance   = b;
    }


    // withdraw()
    // [MODIFIED] Added call to recordTransaction() on success.
    // Original behaviour preserved: returns false if amount < 0 or exceeds balance.
    public boolean withdraw(int amount) {
        if (amount < 0 || balance < amount) {
            return false; // Reject negative amounts and overdrafts
        } else {
            balance = balance - amount; // Subtract from balance

            // [NEW] Record this withdrawal in the transaction history
            recordTransaction(Transaction.TransactionType.WITHDRAWAL, amount);

            return true;
        }
    }


    // deposit()
    // [MODIFIED] Added call to recordTransaction() on success.
    // Original behaviour preserved: returns false if amount is negative.
    public boolean deposit(int amount) {
        if (amount < 0) {
            return false; // Reject negative deposits
        } else {
            balance = balance + amount; // Add to balance

            // [NEW] Record this deposit in the transaction history
            recordTransaction(Transaction.TransactionType.DEPOSIT, amount);

            return true;
        }
    }


    // [NEW] recordTransaction()
    // Protected so subclasses (PrimeAccount, SavingAccount, StudentAccount)
    // can also record their special operations (interest, overdraft fee, etc.)
    // without exposing this to outside code.
    // Keeps the list trimmed to MAX_TRANSACTION_HISTORY to avoid memory bloat.
    // @param type   - the kind of operation (from Transaction.TransactionType enum)
    // @param amount - how much money was involved
    protected void recordTransaction(Transaction.TransactionType type, int amount) {
        // Create a new Transaction snapshot with the current balance AFTER the operation
        transactions.add(new Transaction(type, amount, balance));

        // Trim oldest entries if we've exceeded the maximum history size
        // This removes the very first (oldest) entry when the list gets too long
        if (transactions.size() > MAX_TRANSACTION_HISTORY) {
            transactions.remove(0);
        }
    }


    // [NEW] getMiniStatement()
    // Returns a formatted string showing the last 5 transactions.
    // This is displayed on the ATM screen when the user presses "STMT".
    // If fewer than 5 transactions exist, shows all available.
    // Returns a message if no transactions have been made yet.
    public String getMiniStatement() {
        if (transactions.isEmpty()) {
            return "No transactions found.\nUse the ATM to make\nyour first transaction.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== MINI STATEMENT ===\n");
        sb.append("Account: ").append(accNumber).append("\n");
        sb.append("----------------------\n");

        // Show last 5 transactions (most recent last)
        // Calculate start index so we only show up to 5 entries
        int start = Math.max(0, transactions.size() - 5);
        for (int i = start; i < transactions.size(); i++) {
            sb.append(transactions.get(i).toDisplayString()).append("\n");
        }

        sb.append("----------------------\n");
        sb.append("Current Bal: £").append(balance);
        return sb.toString();
    }


    // [NEW] getTransactionCount()
    // Lets UIModel check whether any transactions have occurred yet.
    // Used to decide whether to show "No history" message.
    public int getTransactionCount() {
        return transactions.size();
    }


    // getBalance() — Getter for the account balance (unchanged)
    // Returns the current balance of this account.
    public int getBalance() {
        return balance;
    }


    // getAccNumber() — Getter for the account number (unchanged)
    public String getAccNumber() {
        return accNumber;
    }


    // getaccPasswd() — Getter for the account password (unchanged)
    // Note: original had lowercase 'a' in getaccPasswd — preserved for compatibility
    public String getaccPasswd() {
        return accPasswd;
    }


    // setBalance()
    // Protected method allowing subclasses (PrimeAccount, SavingAccount)
    // to update the balance directly. Used for overdraft calculations.
    protected void setBalance(int newBalance) {
        this.balance = newBalance;
    }


    // changePassword()
    // Author: Haaroun — Logic to change the password.
    // Step 1: Verify old password matches what we have stored.
    // Step 2: Validate new password (not null, at least 5 characters).
    // Returns true on success, false if old password wrong or new is too short.
    public boolean changePassword(String oldPassword, String newPassword) {
        // 1. Check if the old password provided matches the one we have stored
        if (this.accPasswd.equals(oldPassword)) {

            // 2. Validate: new password must not be empty and at least 5 characters
            if (newPassword != null && newPassword.length() >= 5) {
                this.accPasswd = newPassword; // Update to the new password
                return true;                  // Success!
            }
        }
        // Old password was wrong OR the new one was too short
        return false;
    }


    // [NEW] getAccountTypeName()
    // Returns a human-readable label for the type of account.
    // Overridden by subclasses to show their specific type name.
    // Default (this class) returns "STANDARD".
    public String getAccountTypeName() {
        return "STANDARD";
    }
}
