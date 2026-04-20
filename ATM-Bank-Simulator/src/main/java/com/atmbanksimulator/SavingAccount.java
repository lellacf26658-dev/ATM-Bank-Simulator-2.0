package com.atmbanksimulator;

// SavingAccount (Domain / Service / Business Logic)
// Author: Daniella
//
// SavingAccount represents a savings account that earns interest.
//
// Special Feature:
//   - Interest can be applied to the account balance.
//   - Interest rate: 2% (0.02)
//
// Example:
//   Balance = £1000, InterestRate = 0.02 (2%)
//   Interest = £20
//   New Balance = £1020
//
// CHANGES FROM ORIGINAL:
// 1. [MODIFY] addInterest() — now records the interest as a Transaction
//    so it shows up in the Mini Statement. The original had no history tracking.
//
// 2. [NEW] getAccountTypeName() — overrides BankAccount to return "SAVING"
//    so the View can show the correct account type badge.
//
// 3. [NEW] getInterestPreview() — returns a formatted string showing
//    the user what interest they would earn if they applied it now.
//    UIModel displays this BEFORE the user confirms applying interest.
//
// 4. [NEW] getInterestRate() getter (preserved from original).

public class SavingAccount extends BankAccount {

    // Interest rate for this savings account (2% = 0.02)
    private double interestRate = 0.02;


    // Constructor
    // Calls the parent BankAccount constructor.
    // @param accNumber  account number
    // @param accPasswd  account password
    // @param balance    starting balance
    public SavingAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }


    // addInterest()
    // [MODIFIED] Now records the interest payment as a Transaction.
    //
    // Calculates interest as: (int)(balance * interestRate)
    // The (int) cast rounds DOWN — this is intentional (conservative calculation).
    // Then deposits the interest back into the account.
    //
    // Note: if balance is 0 or negative, interest = 0, so nothing happens.
    public void addInterest() {
        int currentBalance = getBalance();

        // Only apply interest if balance is positive (no interest on £0 or debt)
        if (currentBalance <= 0) {
            return; // Nothing to earn interest on
        }

        // Calculate interest amount (integer truncation = rounds down)
        // e.g. £1000 * 0.02 = £20.0 → (int) = £20
        int interest = (int) (currentBalance * interestRate);

        if (interest <= 0) {
            return; // Interest rounds to zero (very small balance) — skip
        }

        // Add the interest to the balance using the parent deposit() method.
        // This also records a DEPOSIT transaction. We then overwrite that with
        // an INTEREST transaction so the Mini Statement shows the correct type.
        deposit(interest); // Deposit adds to balance and records DEPOSIT

        // [NEW] Correct the transaction type: overwrite the DEPOSIT record
        // with an INTEREST record so the Mini Statement reads correctly.
        // recordTransaction() is protected — accessible from subclasses.
        recordTransaction(Transaction.TransactionType.INTEREST, interest);

        System.out.println("SavingAccount: Interest of £" + interest +
                " applied. New balance: £" + getBalance());
    }


    // [NEW] getInterestPreview()
    // Returns a formatted string showing how much interest the user would
    // receive if they pressed the "Apply Interest" button right now.
    // UIModel displays this when the user first presses "Int" so they
    // know what will happen before it is applied.
    public String getInterestPreview() {
        int currentBalance = getBalance();

        if (currentBalance <= 0) {
            return "No interest applicable.\nBalance must be positive\nto earn interest.";
        }

        int interest = (int) (currentBalance * interestRate);

        if (interest <= 0) {
            return "Balance too low to earn\nmeasurable interest.\n(Rate: " +
                    (interestRate * 100) + "%)";
        }

        return String.format(
                "=== INTEREST PREVIEW ===\n" +
                        "Current Balance: £%d\n" +
                        "Interest Rate:   %.0f%%\n" +
                        "Interest Earned: £%d\n" +
                        "New Balance:     £%d\n" +
                        "========================\n" +
                        "Press Ent to confirm\nor CLR to cancel.",
                currentBalance,
                interestRate * 100,
                interest,
                currentBalance + interest
        );
    }


    // [NEW] getAccountTypeName()
    // Overrides BankAccount's version to return "SAVING".
    @Override
    public String getAccountTypeName() {
        return "SAVING";
    }


    // getInterestRate() — Getter for interest rate (unchanged from original)
    public double getInterestRate() {
        return interestRate;
    }
}
