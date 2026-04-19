package com.atmbanksimulator;

// NEW CLASS: Transaction 
//
// This class represents a single financial transaction record.
// It is used by BankAccount to build a history of all operations,
// which powers the "Mini Statement" feature.
// Each transaction captures: what happened, how much, and when.

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {


    // ENUM: TransactionType
    // Defines every possible kind of transaction the ATM can perform.
    // Using an enum prevents typos and makes switch-statements type-safe.
    public enum TransactionType {
        DEPOSIT,        // Money added to account
        WITHDRAWAL,     // Money taken from account
        TRANSFER_OUT,   // Money sent to another account
        TRANSFER_IN,    // Money received from another account
        INTEREST,       // Interest credited (SavingAccount feature)
        OVERDRAFT_FEE   // Fee charged for going into overdraft (PrimeAccount feature - future use)
    }


    // INSTANCE VARIABLES
    // These store the details of one specific transaction event.
    private final TransactionType type;       // What kind of transaction
    private final int             amount;      // How much money (in pence or pounds - integer)
    private final int             balanceAfter;// Account balance AFTER this transaction completed
    private final LocalDateTime   timestamp;   // Exact date and time the transaction occurred


    // CONSTRUCTOR
    // Called by BankAccount whenever a financial operation succeeds.
    // The timestamp is automatically set to "right now".
    // @param type         - the kind of operation (DEPOSIT, WITHDRAWAL, etc.)
    // @param amount       - the monetary value involved
    // @param balanceAfter - the account balance immediately after this operation
    public Transaction(TransactionType type, int amount, int balanceAfter) {
        this.type         = type;
        this.amount       = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp    = LocalDateTime.now(); // Captured automatically at time of creation
    }


    // toDisplayString()
    // Formats this transaction as a single readable line for the Mini Statement screen.
    // Example output: "18/04 14:32  DEPOSIT   £200  [Bal: £750]"
    public String toDisplayString() {
        // Format the timestamp to just show date and time (no year, saves screen space)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        String timeStr = timestamp.format(fmt);

        // Short labels for each transaction type
        String typeLabel = switch (type) {
            case DEPOSIT        -> "DEPOSIT  ";
            case WITHDRAWAL     -> "WITHDRAW ";
            case TRANSFER_OUT   -> "TRF OUT  ";
            case TRANSFER_IN    -> "TRF IN   ";
            case INTEREST       -> "INTEREST ";
            case OVERDRAFT_FEE  -> "OVD FEE  ";
        };

        // Build the full display line, padding for column alignment
        return String.format("%s  %s  +/-£%-6d  Bal:£%d",
                timeStr, typeLabel, amount, balanceAfter);
    }


    // GETTERS — Read-only access to transaction data
    // These are used by getMiniStatement() in BankAccount

    /** Returns the type of transaction (DEPOSIT, WITHDRAWAL, etc.) */
    public TransactionType getType() {
        return type;
    }

    /** Returns the amount of money involved in this transaction */
    public int getAmount() {
        return amount;
    }

    /** Returns the account balance immediately after this transaction */
    public int getBalanceAfter() {
        return balanceAfter;
    }

    /** Returns the exact date and time this transaction was recorded */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
