package com.atmbanksimulator;

// Bank 
// CHANGES FROM ORIGINAL:
// 1. [FIX]    Replaced BankAccount[10] array with ArrayList<BankAccount>
//             — removes the 10-account hard limit; bank can hold unlimited accounts
//             — removes need for numAccounts counter and maxAccounts constant
//             — addBankAccount() simplified to ArrayList.add()
//             — login() loop simplified (no null checks needed in ArrayList)
// 2. [NEW]    Login attempt tracking per account number (HashMap<String,Integer>)
//             — after 3 failed attempts the account is locked
// 3. [NEW]    Account locking (HashSet<String> of locked account numbers)
//             — isAccountLocked() public method for UIModel to query
//             — lockAccount() called automatically on 3rd failed attempt
// 4. [NEW]    Currency exchange support
//             — static Map of exchange rates (GBP → USD, EUR, JPY, AED, CAD)
//             — convertCurrency(int gbpAmount, String currency) returns formatted string
//             — getAvailableCurrencies() lists all supported currencies
// 5. [NEW]    getLoggedInAccount() — lets UIModel access the logged-in BankAccount object
//             directly (needed for instanceof checks, e.g. is it a SavingAccount?)
// 6. [NEW]    getAccountByNumber() — lets UIModel look up accounts for display purposes
// 7. [KEEP]   All original methods preserved (makeBankAccount, login, logout, deposit,
//             withdraw, getBalance, transfer, changePassword)

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Bank {

    // Enum representing different types of bank accounts (unchanged from original)
    public enum AccountType {
        STANDARD, STUDENT, PRIME, SAVING
    }


    // [MODIFIED] Instance variables — accounts storage
    // REMOVED: private int maxAccounts = 10;
    // REMOVED: private int numAccounts = 0;
    // REMOVED: private BankAccount[] accounts = new BankAccount[maxAccounts];

    // ADDED: ArrayList — dynamically sized, no null-check loops needed
    private ArrayList<BankAccount> accounts = new ArrayList<>();

    // The account currently logged in (null if no one is logged in) — unchanged
    private BankAccount loggedInAccount = null;

    // [NEW] Login security: attempt tracking and account locking

    // loginAttempts: maps each account number to how many times
    //   it has failed to log in since the ATM session started.
    //   Key = account number (String), Value = fail count (Integer)

    // lockedAccounts: a set of account numbers that have been locked
    //   after too many failed attempts. An account in this set
    //   cannot be logged into until it is unlocked (not yet implemented
    //   in this version — would require admin/bank staff action).

    private HashMap<String, Integer> loginAttempts = new HashMap<>();
    private HashSet<String>          lockedAccounts = new HashSet<>();

    // How many failed login attempts before the account is locked.
    // Stored as a constant so it's easy to change in one place.
    private static final int MAX_LOGIN_ATTEMPTS = 3;


    // [NEW] Currency exchange rates
    // Maps currency code (e.g. "USD") to its exchange rate from GBP.
    // Example: 1 GBP = 1.27 USD, so rate for USD = 1.27
    // These are approximate rates — a real system would fetch live rates via API.
    private static final Map<String, Double> EXCHANGE_RATES = new HashMap<>();
    static {
        // Populate exchange rates (GBP as base currency)
        EXCHANGE_RATES.put("USD", 1.27);  // US Dollar
        EXCHANGE_RATES.put("EUR", 1.17);  // Euro
        EXCHANGE_RATES.put("JPY", 191.5); // Japanese Yen
        EXCHANGE_RATES.put("AED", 4.66);  // UAE Dirham
        EXCHANGE_RATES.put("CAD", 1.73);  // Canadian Dollar
    }


    // makeBankAccount() — Factory method to create a BankAccount with no type
    // (unchanged from original)
    public BankAccount makeBankAccount(String accNumber, String accPasswd, int balance) {
        return new BankAccount(accNumber, accPasswd, balance);
    }


    // makeBankAccount() — Factory method overload: creates specific subclass
    // based on AccountType enum. Uses switch to instantiate correct class.
    // (unchanged from original)
    public BankAccount makeBankAccount(String accNumber, String accPasswd,
                                       int balance, AccountType type) {
        switch (type) {
            case STUDENT: return new StudentAccount(accNumber, accPasswd, balance);
            case PRIME:   return new PrimeAccount(accNumber, accPasswd, balance);
            case SAVING:  return new SavingAccount(accNumber, accPasswd, balance);
            default:      return new BankAccount(accNumber, accPasswd, balance);
        }
    }


    // addBankAccount(BankAccount a)
    // [MODIFIED] Simplified — ArrayList.add() always succeeds (no capacity limit).
    // The original returned false when the array was full; that can never happen now.
    // We keep the boolean return type for API compatibility but always return true.
    public boolean addBankAccount(BankAccount a) {
        accounts.add(a); // ArrayList grows automatically — no size check needed
        return true;
    }


    // addBankAccount(String, String, int) — Convenience overload, no type
    // [MODIFIED] Delegates to the BankAccount overload above (unchanged logic)
    public boolean addBankAccount(String accNumber, String accPasswd, int balance) {
        return addBankAccount(makeBankAccount(accNumber, accPasswd, balance));
    }


    // addBankAccount(String, String, int, AccountType) — With account type
    // [MODIFIED] Delegates to the typed factory method (unchanged logic)
    public boolean addBankAccount(String accNumber, String accPasswd,
                                  int balance, AccountType type) {
        return addBankAccount(makeBankAccount(accNumber, accPasswd, balance, type));
    }


    // login()
    // [MODIFIED] Added account-lock check and attempt tracking.
    // Flow:
    //   1. First logout any currently logged-in account
    //   2. Check if the requested account is locked — refuse if so
    //   3. Search accounts for matching number AND password
    //   4. On success: reset attempt counter, set loggedInAccount, return true
    //   5. On failure: increment attempt counter; lock account if limit reached
    //
    // ORIGINAL logic (search loop + loggedInAccount assignment) is preserved.
    public boolean login(String accountNumber, String password) {
        logout(); // Always log out any previous session first

        // [NEW] Check if this account has been locked due to too many failed attempts
        if (lockedAccounts.contains(accountNumber)) {
            // Return false — the UIModel will display the lock message separately
            // by calling isAccountLocked() after a failed login
            return false;
        }

        // Search the accounts list for a matching account number AND password
        // [MODIFIED] No null check needed — ArrayList never contains null entries
        for (BankAccount b : accounts) {
            if (b.getAccNumber().equals(accountNumber) &&
                    b.getaccPasswd().equals(password)) {

                // Match found — log in successfully
                loggedInAccount = b;

                // [NEW] Reset the failed attempt counter for this account on success
                loginAttempts.put(accountNumber, 0);

                return true;
            }
        }

        // No match found — record a failed attempt
        // [NEW] Increment the attempt counter for this account number
        int attempts = loginAttempts.getOrDefault(accountNumber, 0) + 1;
        loginAttempts.put(accountNumber, attempts);

        // [NEW] Lock the account if the maximum attempts have been reached
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            lockedAccounts.add(accountNumber);
            System.out.println("Bank: Account " + accountNumber +
                    " LOCKED after " + attempts + " failed attempts.");
        }

        loggedInAccount = null;
        return false;
    }


    // logout() — Clears the logged-in account (unchanged from original)
    public void logout() {
        if (loggedIn()) {
            loggedInAccount = null;
        }
    }


    // loggedIn() — Returns true if someone is currently logged in (unchanged)
    public boolean loggedIn() {
        return loggedInAccount != null;
    }


    // deposit() — Deposits into the logged-in account (unchanged)
    public boolean deposit(int amount) {
        if (loggedIn()) {
            return loggedInAccount.deposit(amount);
        } else {
            return false;
        }
    }


    // withdraw() — Withdraws from the logged-in account (unchanged)
    public boolean withdraw(int amount) {
        if (loggedIn()) {
            return loggedInAccount.withdraw(amount);
        } else {
            return false;
        }
    }


    // getBalance() — Returns balance of logged-in account (unchanged)
    // Returns -1 as error indicator if not logged in.
    public int getBalance() {
        if (loggedIn()) {
            return loggedInAccount.getBalance();
        } else {
            return -1; // Error indicator — UIModel checks for this
        }
    }


    // transfer()
    // Author: Daniella (unchanged logic)
    // Transfers money from the logged-in account to another account.
    // Returns false if: not logged in, target not found, or withdrawal fails.
    public boolean transfer(String targetAccountNumber, int amount) {
        if (!loggedIn()) {
            return false; // Must be logged in to transfer
        }

        // Find the target account in the accounts list
        BankAccount target = null;
        for (BankAccount acc : accounts) {
            if (acc.getAccNumber().equals(targetAccountNumber)) {
                target = acc;
                break;
            }
        }

        if (target == null) {
            return false; // Target account does not exist
        }

        // [FIX] Prevent transferring to your own account
        if (target == loggedInAccount) {
            return false;
        }

        // Attempt to withdraw from the current (logged-in) account
        if (loggedInAccount.withdraw(amount)) {
            // Withdrawal succeeded — record as TRANSFER_OUT (overwrites the WITHDRAWAL
            // that withdraw() already recorded, by adding a corrective entry)
            loggedInAccount.recordTransaction(
                    Transaction.TransactionType.TRANSFER_OUT, amount);

            // Deposit into the target account and record as TRANSFER_IN
            target.deposit(amount);
            target.recordTransaction(Transaction.TransactionType.TRANSFER_IN, amount);

            return true;
        }

        return false; // Withdrawal failed (insufficient funds, etc.)
    }


    // changePassword()
    // Author: Haaroun (unchanged — bridge to BankAccount.changePassword)
    public boolean changePassword(String old, String newP) {
        if (loggedIn()) {
            return loggedInAccount.changePassword(old, newP);
        }
        return false;
    }


    // [NEW] getLoggedInAccount()
    // Exposes the logged-in BankAccount object to UIModel.
    // UIModel uses this for instanceof checks — e.g. to test whether
    // the logged-in account is a SavingAccount (so it can offer
    // the "Apply Interest" button), or a StudentAccount (to show
    // remaining daily limit), etc.
    // Returns null if no one is logged in.
    public BankAccount getLoggedInAccount() {
        return loggedInAccount;
    }


    // [NEW] isAccountLocked()
    // UIModel calls this after a failed login() to decide what message
    // to display to the user — "wrong password" vs "account locked".
    // @param accountNumber — the account number that was attempted
    // @return true if this account is in the locked set
    public boolean isAccountLocked(String accountNumber) {
        return lockedAccounts.contains(accountNumber);
    }


    // [NEW] getRemainingLoginAttempts()
    // UIModel uses this to warn the user how many attempts they have left.
    // E.g. "Incorrect password. 2 attempts remaining."
    // @param accountNumber — the account number that was just attempted
    // @return number of attempts still allowed (0 if locked)
    public int getRemainingLoginAttempts(String accountNumber) {
        int used = loginAttempts.getOrDefault(accountNumber, 0);
        return Math.max(0, MAX_LOGIN_ATTEMPTS - used);
    }


    // [NEW] convertCurrency()
    // Converts a GBP amount to the specified target currency.
    // Returns a formatted multi-line string showing the conversion result,
    // ready to be displayed directly on the ATM screen.
    // @param gbpAmount  — the amount in GBP (whole pounds, as entered by user)
    // @param currency   — the target currency code (e.g. "USD", "EUR")
    // @return formatted result string, or an error message if currency not found
    public String convertCurrency(int gbpAmount, String currency) {
        String cur = currency.toUpperCase().trim();

        if (!EXCHANGE_RATES.containsKey(cur)) {
            return "Currency '" + cur + "' not supported.\n" +
                    "Supported: " + getAvailableCurrencies();
        }

        double rate   = EXCHANGE_RATES.get(cur);
        double result = gbpAmount * rate;

        // Format result: JPY has no decimals (large numbers), others show 2dp
        String resultStr;
        if (cur.equals("JPY")) {
            resultStr = String.format("%.0f", result);
        } else {
            resultStr = String.format("%.2f", result);
        }

        return String.format(
                "=== CURRENCY EXCHANGE ===\n" +
                        "Amount:  GBP £%d\n" +
                        "Rate:    1 GBP = %.4f %s\n" +
                        "Result:  %s %s\n" +
                        "========================\n" +
                        "(Indicative rate only.\nRates updated periodically.)",
                gbpAmount, rate, cur, resultStr, cur
        );
    }


    // [NEW] getAvailableCurrencies()
    // Returns a comma-separated list of all supported currency codes.
    // Shown to the user when they select the currency exchange feature.
    public String getAvailableCurrencies() {
        return String.join(", ", EXCHANGE_RATES.keySet());
    }
}
