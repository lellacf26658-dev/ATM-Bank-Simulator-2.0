package com.atmbanksimulator;

// UIModel
//
// The UIModel holds all state and logic for the ATM session.
// It sits between the Controller (which receives button presses)
// and the Bank/BankAccount (which does the actual financial work).
//
// CHANGES FROM ORIGINAL:
// 1. [NEW]    STATE_TRANSFER_ACC — first step of transfer: entering target account number
// 2. [NEW]    STATE_TRANSFER_AMT — second step of transfer: entering the amount
//             (Original used a single state with a flag; this is cleaner)
// 3. [NEW]    STATE_MINI_STMT    — displays the last 5 transactions
// 4. [NEW]    STATE_CURRENCY     — currency exchange amount entry
// 5. [NEW]    STATE_CURRENCY_SEL — currency selection step
// 6. [NEW]    STATE_INTEREST     — confirm applying interest (SavingAccount only)
// 7. [NEW]    transferTargetAcc  — stores target account number during transfer flow
// 8. [NEW]    selectedCurrency   — stores the chosen currency code during exchange flow
// 9. [NEW]    Getters for all private fields so View can read them without direct access
// 10.[NEW]    processMiniStmt(), processCurrency(), processInterest() methods
// 11.[MODIFY] processEnter() — extended to handle all new states
// 12.[MODIFY] processTransfer() — now uses two-step STATE_TRANSFER_ACC → STATE_TRANSFER_AMT
// 13.[MODIFY] processWithdraw(), processDeposit() — show balance after transaction
//             and show StudentAccount daily limit info if applicable
// 14.[MODIFY] processEnter() for login — shows remaining attempts or lock message

public class UIModel {

    // Reference to the View (part of the MVC setup) — unchanged
    View view;

    // The ATM communicates with this Bank object — unchanged
    private Bank bank;


    // STATE CONSTANTS
    // The ATM finite-state machine (FSM) can be in one of these states.
    // Using String constants (not an enum) to stay close to the original design.
    // [NEW states added below the original four]
    private final String STATE_ACCOUNT_NO   = "account_no";   // Waiting for account number input
    private final String STATE_PASSWORD     = "password";     // Waiting for password input
    private final String STATE_LOGGED_IN    = "logged_in";    // User is logged in, menu active
    private final String STATE_CH_PASS      = "change_password"; // Author: Haaroun
    private final String STATE_TRANSFER_ACC = "transfer_acc"; // [NEW] Step 1: enter target acc no.
    private final String STATE_TRANSFER_AMT = "transfer_amt"; // [NEW] Step 2: enter transfer amount
    private final String STATE_MINI_STMT    = "mini_stmt";    // [NEW] Displaying mini statement
    private final String STATE_CURRENCY     = "currency_amt"; // [NEW] Enter amount for conversion
    private final String STATE_CURRENCY_SEL = "currency_sel"; // [NEW] Choosing currency (1-5)
    private final String STATE_INTEREST     = "interest";     // [NEW] Confirm applying interest


    // INSTANCE VARIABLES — ATM session data
    private String state          = STATE_ACCOUNT_NO; // Current state of the ATM FSM
    private String accNumber      = "";               // Account number being typed
    private String accPasswd      = "";               // Password being typed
    private String oldPasswd      = "";               // Author: Haaroun — for changePassword flow
    private String message        = "";               // Single-line status/instruction label
    private String numberPadInput = "";               // Number shown in the TextField
    private String result         = "";               // Multiline output in the TextArea

    // [NEW] Stores the target account number during a two-step transfer
    private String transferTargetAcc = "";

    // [NEW] Stores the selected currency code (e.g. "USD") during exchange flow
    private String selectedCurrency = "";


    // Constructor — receives the Bank this UIModel will communicate with
    // (unchanged from original)
    public UIModel(Bank bank) {
        this.bank = bank;
    }


    // initialise()
    // Called by Main when the application starts.
    // Sets the ATM to its initial waiting-for-account-number state.
    // (unchanged from original)
    public void initialise() {
        setState(STATE_ACCOUNT_NO);
        numberPadInput = "";
        message = "Welcome to FIN Bank ATM";
        result  = "Please enter your account\nnumber, then press Ent.";
        update();
    }


    // reset()
    // Resets the ATM to the account-number entry state with a given message.
    // Called after logout, errors, or when the user presses Fin.
    // (unchanged from original — private access)
    private void reset(String msg) {
        setState(STATE_ACCOUNT_NO);
        numberPadInput = "";
        message = msg;
        result  = "Please enter your account\nnumber, then press Ent.";
    }


    // setState()
    // Changes the ATM state and prints a debug message to the console.
    // (unchanged from original — private access)
    private void setState(String newState) {
        if (!state.equals(newState)) {
            String oldState = state;
            state = newState;
            System.out.println("UIModel::setState: changed state from "
                    + oldState + " to " + newState);
        }
    }


    // PROCESS METHODS — Called by the Controller in response to button presses


    // processNumber()
    // Author: Haaroun — added length check to prevent UI overflow.
    // Appends a digit to numberPadInput (up to 8 digits).
    // (unchanged from original)
    public void processNumber(String numberOnButton) {
        if (numberPadInput.length() < 8) {
            numberPadInput += numberOnButton;
            message = ""; // Clear any previous message to keep UI clean
            result  = ""; // Clear result area on new input
        } else {
            message = "Input too long!";
            result  = "Maximum 8 digits allowed.";
        }
        update();
    }


    // processClear()
    // Clears the current numberPadInput without changing state.
    // Useful if the user mis-typed a digit.
    // (unchanged from original)
    public void processClear() {
        numberPadInput = "";
        message = "Cleared.";
        result  = "";
        update();
    }


    // processEnter()
    // [MODIFIED] — handles all states including new ones.
    // This is the main "confirm" button. What it does depends on
    // the current state (FSM). Each case is documented below.
    public void processEnter() {

        // STATE: Waiting for account number
        if (state.equals(STATE_ACCOUNT_NO)) {
            if (numberPadInput.isEmpty()) {
                message = "Please enter your account number.";
                result  = "";
            } else {
                // Store the typed account number and move to password state
                accNumber      = numberPadInput;
                numberPadInput = "";
                setState(STATE_PASSWORD);
                message = "Account: " + accNumber;
                result  = "Enter your PIN / password,\nthen press Ent.";
            }

            // STATE: Waiting for password
        } else if (state.equals(STATE_PASSWORD)) {
            if (numberPadInput.isEmpty()) {
                message = "Please enter your password.";
                result  = "";
            } else {
                accPasswd      = numberPadInput;
                numberPadInput = "";

                // Attempt login via Bank
                boolean success = bank.login(accNumber, accPasswd);

                if (success) {
                    // [NEW] Greet the user with their account type
                    BankAccount acc = bank.getLoggedInAccount();
                    setState(STATE_LOGGED_IN);
                    message = "Welcome! (" + acc.getAccountTypeName() + " Account)";
                    result  = buildLoggedInMenu();
                } else {
                    // [NEW] Check if the account is now locked after this failure
                    if (bank.isAccountLocked(accNumber)) {
                        setState(STATE_ACCOUNT_NO);
                        message = "Account LOCKED.";
                        result  = "Too many failed attempts.\n" +
                                "Please contact your bank\nto unlock your account.";
                    } else {
                        // Show how many attempts remain
                        int remaining = bank.getRemainingLoginAttempts(accNumber);
                        setState(STATE_ACCOUNT_NO);
                        message = "Incorrect password.";
                        result  = "Please try again.\n" +
                                "Attempts remaining: " + remaining + "\n" +
                                "Enter account number:";
                    }
                }
            }

            // STATE: Logged in (number pad used for amounts)
            // In this state, pressing Ent with a number does nothing special —
            // the user should press a function button (W/D, Dep, etc.) first.
        } else if (state.equals(STATE_LOGGED_IN)) {
            if (!numberPadInput.isEmpty()) {
                message = "Select an action first.";
                result  = buildLoggedInMenu();
                numberPadInput = "";
            } else {
                message = "Select an option from\nthe menu on the right.";
                result  = buildLoggedInMenu();
            }

            // STATE: Change Password — step 1 (enter old password)
            // Author: Haaroun
        } else if (state.equals(STATE_CH_PASS)) {
            if (numberPadInput.isEmpty()) {
                message = "Enter old password.";
            } else {
                // First press: store old password, ask for new one
                if (oldPasswd.isEmpty()) {
                    oldPasswd      = numberPadInput;
                    numberPadInput = "";
                    message = "Now enter new password\n(min 5 characters):";
                    result  = "";
                } else {
                    // Second press: new password entered — attempt change
                    String newPass = numberPadInput;
                    numberPadInput = "";

                    boolean changed = bank.changePassword(oldPasswd, newPass);
                    oldPasswd = ""; // Clear stored old password for security

                    if (changed) {
                        setState(STATE_LOGGED_IN);
                        message = "Password changed!";
                        result  = "Your password has been\nupdated successfully.\n\n"
                                + buildLoggedInMenu();
                    } else {
                        setState(STATE_LOGGED_IN);
                        message = "Password change failed.";
                        result  = "Old password incorrect or\nnew password too short.\n\n"
                                + buildLoggedInMenu();
                    }
                }
            }

            // [NEW] STATE: Transfer — Step 1 (enter target account number)
        } else if (state.equals(STATE_TRANSFER_ACC)) {
            if (numberPadInput.isEmpty()) {
                message = "Enter recipient's account number.";
            } else {
                // Store target account number, move to amount entry
                transferTargetAcc = numberPadInput;
                numberPadInput    = "";
                setState(STATE_TRANSFER_AMT);
                message = "Recipient: " + transferTargetAcc;
                result  = "Enter amount to transfer,\nthen press Ent.";
            }

            // [NEW] STATE: Transfer — Step 2 (enter amount)
        } else if (state.equals(STATE_TRANSFER_AMT)) {
            if (numberPadInput.isEmpty()) {
                message = "Enter amount to transfer.";
            } else {
                try {
                    int amount = Integer.parseInt(numberPadInput);
                    numberPadInput = "";

                    boolean success = bank.transfer(transferTargetAcc, amount);
                    transferTargetAcc = ""; // Clear after use

                    setState(STATE_LOGGED_IN);
                    if (success) {
                        message = "Transfer successful!";
                        result  = "Transferred £" + amount + "\nNew balance: £"
                                + bank.getBalance() + "\n\n" + buildLoggedInMenu();
                    } else {
                        message = "Transfer failed.";
                        result  = "Account not found or\ninsufficient funds.\n\n"
                                + buildLoggedInMenu();
                    }
                } catch (NumberFormatException e) {
                    message = "Invalid amount.";
                    result  = "Please enter numbers only.";
                    numberPadInput = "";
                }
            }

            // [NEW] STATE: Currency Exchange — Step 1 (choose currency)
        } else if (state.equals(STATE_CURRENCY_SEL)) {
            if (numberPadInput.isEmpty()) {
                message = "Enter a currency number (1-5).";
            } else {
                // Map the number the user typed to a currency code
                String[] currencies = {"USD", "EUR", "JPY", "AED", "CAD"};
                try {
                    int choice = Integer.parseInt(numberPadInput);
                    numberPadInput = "";

                    if (choice >= 1 && choice <= currencies.length) {
                        selectedCurrency = currencies[choice - 1];
                        setState(STATE_CURRENCY);
                        message = "Converting to: " + selectedCurrency;
                        result  = "Enter GBP amount to convert,\nthen press Ent.";
                    } else {
                        message = "Invalid choice. Enter 1-5.";
                        result  = buildCurrencyMenu();
                    }
                } catch (NumberFormatException e) {
                    numberPadInput = "";
                    message = "Enter a number (1-5).";
                    result  = buildCurrencyMenu();
                }
            }

            // [NEW] STATE: Currency Exchange — Step 2 (enter GBP amount)
        } else if (state.equals(STATE_CURRENCY)) {
            if (numberPadInput.isEmpty()) {
                message = "Enter amount in GBP.";
            } else {
                try {
                    int gbpAmount = Integer.parseInt(numberPadInput);
                    numberPadInput = "";

                    // Delegate the conversion to Bank
                    String convResult = bank.convertCurrency(gbpAmount, selectedCurrency);
                    selectedCurrency = ""; // Clear after use

                    setState(STATE_LOGGED_IN);
                    message = "Currency Exchange Result";
                    result  = convResult + "\n\n" + buildLoggedInMenu();
                } catch (NumberFormatException e) {
                    numberPadInput = "";
                    message = "Invalid amount. Enter numbers only.";
                }
            }

            // [NEW] STATE: Confirm Apply Interest (SavingAccount only)
        } else if (state.equals(STATE_INTEREST)) {
            // The user pressed Ent to confirm — apply the interest now
            BankAccount acc = bank.getLoggedInAccount();
            if (acc instanceof SavingAccount) {
                SavingAccount saving = (SavingAccount) acc;
                int balanceBefore = saving.getBalance();
                saving.addInterest();
                int balanceAfter = saving.getBalance();
                int earned = balanceAfter - balanceBefore;

                setState(STATE_LOGGED_IN);
                message = "Interest applied!";
                result  = "Interest earned: £" + earned
                        + "\nNew balance:    £" + balanceAfter
                        + "\n\n" + buildLoggedInMenu();
            } else {
                setState(STATE_LOGGED_IN);
                message = "Not a Savings Account.";
                result  = buildLoggedInMenu();
            }
        }

        update();
    }


    // processWithdraw()
    // [MODIFIED] Added post-transaction balance display.
    // Also shows StudentAccount daily limit info if applicable.
    public void processWithdraw() {
        if (!bank.loggedIn()) { reset("Not logged in."); update(); return; }

        if (numberPadInput.isEmpty()) {
            message = "Enter amount, then W/D";
            result  = "Type the amount to withdraw\nthen press W/D again.";
        } else {
            try {
                int amount = Integer.parseInt(numberPadInput);
                numberPadInput = "";

                boolean success = bank.withdraw(amount);

                if (success) {
                    message = "Withdrawn: £" + amount;

                    // [NEW] Show balance after withdrawal, and daily limit for students
                    String extra = "";
                    BankAccount acc = bank.getLoggedInAccount();
                    if (acc instanceof StudentAccount) {
                        extra = "\n\n" + ((StudentAccount) acc).getDailyLimitInfo();
                    } else if (acc instanceof PrimeAccount) {
                        PrimeAccount prime = (PrimeAccount) acc;
                        if (prime.getBalance() < 0) {
                            extra = "\n(Overdraft active)\nRemaining overdraft: £"
                                    + prime.getOverdraftRemaining();
                        }
                    }

                    result = "New balance: £" + bank.getBalance()
                            + extra + "\n\n" + buildLoggedInMenu();
                } else {
                    message = "Withdrawal failed.";
                    result  = "Insufficient funds.\nBalance: £" + bank.getBalance()
                            + "\n\n" + buildLoggedInMenu();
                }
            } catch (NumberFormatException e) {
                message = "Invalid amount.";
                result  = "Please enter numbers only.\n\n" + buildLoggedInMenu();
                numberPadInput = "";
            }
        }
        update();
    }


    // processDeposit()
    // [MODIFIED] Added post-transaction balance display.
    public void processDeposit() {
        if (!bank.loggedIn()) { reset("Not logged in."); update(); return; }

        if (numberPadInput.isEmpty()) {
            message = "Enter amount, then Dep";
            result  = "Type the amount to deposit\nthen press Dep again.";
        } else {
            try {
                int amount = Integer.parseInt(numberPadInput);
                numberPadInput = "";

                boolean success = bank.deposit(amount);

                if (success) {
                    message = "Deposited: £" + amount;
                    result  = "New balance: £" + bank.getBalance()
                            + "\n\n" + buildLoggedInMenu();
                } else {
                    message = "Deposit failed.";
                    result  = "Invalid amount.\n\n" + buildLoggedInMenu();
                }
            } catch (NumberFormatException e) {
                message = "Invalid amount.";
                result  = "Please enter numbers only.\n\n" + buildLoggedInMenu();
                numberPadInput = "";
            }
        }
        update();
    }


    // processBalance()
    // [MODIFIED] Shows account type and overdraft info where relevant.
    public void processBalance() {
        if (!bank.loggedIn()) { reset("Not logged in."); update(); return; }

        int bal = bank.getBalance();
        BankAccount acc = bank.getLoggedInAccount();

        String extra = "";
        if (acc instanceof PrimeAccount && bal < 0) {
            PrimeAccount prime = (PrimeAccount) acc;
            extra = "\n(Overdraft)\nRemaining overdraft: £" + prime.getOverdraftRemaining();
        } else if (acc instanceof StudentAccount) {
            extra = "\n" + ((StudentAccount) acc).getDailyLimitInfo();
        } else if (acc instanceof SavingAccount) {
            SavingAccount sa = (SavingAccount) acc;
            int preview = (int)(bal * sa.getInterestRate());
            extra = "\nPotential interest: £" + preview;
        }

        message = "Current Balance";
        result  = "Account: " + acc.getAccNumber()
                + "\nType:    " + acc.getAccountTypeName()
                + "\nBalance: £" + bal
                + extra
                + "\n\n" + buildLoggedInMenu();
        update();
    }


    // processTransfer()
    // [MODIFIED] Now triggers a two-step flow using STATE_TRANSFER_ACC
    // and STATE_TRANSFER_AMT, instead of the original single-step approach.
    // The user must first enter the target account number, then the amount.
    public void processTransfer() {
        if (!bank.loggedIn()) { reset("Not logged in."); update(); return; }

        setState(STATE_TRANSFER_ACC);
        numberPadInput    = "";
        transferTargetAcc = "";
        message = "Transfer Money";
        result  = "Enter the recipient's\naccount number,\nthen press Ent.";
        update();
    }


    // processChP()
    // [UNCHANGED from original — Author: Haaroun]
    // Triggers the change-password flow.
    public void processChP() {
        if (!bank.loggedIn()) { reset("Not logged in."); update(); return; }

        setState(STATE_CH_PASS);
        numberPadInput = "";
        oldPasswd      = ""; // Clear any leftover old password data
        message = "Change Password";
        result  = "Enter your CURRENT\npassword, then press Ent.";
        update();
    }


    // [NEW] processMiniStmt()
    // Triggered when the user presses the "STMT" button.
    // Retrieves and displays the last 5 transactions for the logged-in account.
    // Author: Reema (Mini Statement — Week 3-4 task)
    public void processMiniStmt() {
        if (!bank.loggedIn()) { reset("Not logged in."); update(); return; }

        setState(STATE_MINI_STMT);
        numberPadInput = "";

        BankAccount acc = bank.getLoggedInAccount();
        message = "Mini Statement";
        result  = acc.getMiniStatement();
        update();
    }


    // [NEW] processCurrency()
    // Triggered when the user presses the "CUR" button.
    // Starts the two-step currency exchange flow:
    //   Step 1: user picks a currency (1-5)
    //   Step 2: user enters an amount in GBP
    // Group IDEAS TO ADD list — "Exchange currency"
    public void processCurrency() {
        if (!bank.loggedIn()) { reset("Not logged in."); update(); return; }

        setState(STATE_CURRENCY_SEL);
        numberPadInput   = "";
        selectedCurrency = "";
        message = "Currency Exchange";
        result  = buildCurrencyMenu();
        update();
    }


    // [NEW] processInterest()
    // Triggered when the user presses the "INT" button.
    // Only works for SavingAccount holders — shows a preview first.
    // User presses Ent to confirm, or CLR to cancel.
    public void processInterest() {
        if (!bank.loggedIn()) { reset("Not logged in."); update(); return; }

        BankAccount acc = bank.getLoggedInAccount();

        if (acc instanceof SavingAccount) {
            setState(STATE_INTEREST);
            numberPadInput = "";
            message = "Apply Interest";
            result  = ((SavingAccount) acc).getInterestPreview();
        } else {
            // Not a savings account — explain why this feature is unavailable
            message = "Not available.";
            result  = "Interest is only available\nfor Savings Accounts.\n"
                    + "Your account type: " + acc.getAccountTypeName()
                    + "\n\n" + buildLoggedInMenu();
        }
        update();
    }


    // processFinish()
    // Logs out the current user and resets the ATM to the initial state.
    // (unchanged from original)
    public void processFinish() {
        bank.logout();
        reset("Thank you for using\nFIN Bank ATM.");
        update();
    }


    // processUnknownKey()
    // Called for any unrecognised action string (fallback case in Controller).
    // (unchanged from original)
    public void processUnknownKey(String action) {
        message = "Unknown action: " + action;
        result  = "";
        update();
    }


    // update()
    // Tells the View to refresh its display by reading the latest
    // state from this UIModel. Called at the end of every process method.
    // (unchanged from original)
    private void update() {
        view.update();
    }


    // HELPER METHODS (Private)


    // [NEW] buildLoggedInMenu()
    // Returns the standard post-login menu text shown in the result area.
    // Having this as a helper prevents code duplication across many methods.
    private String buildLoggedInMenu() {
        BankAccount acc = bank.getLoggedInAccount();
        String type = (acc != null) ? acc.getAccountTypeName() : "";
        return "──────────────────────\n"
                + "BAL  Check Balance\n"
                + "W/D  Withdraw\n"
                + "DEP  Deposit\n"
                + "TRF  Transfer\n"
                + "ChP  Change Password\n"
                + "STMT Mini Statement\n"
                + "CUR  Currency Exchange\n"
                + (type.equals("SAVING") ? "INT  Apply Interest\n" : "")
                + "FIN  Log Out\n"
                + "──────────────────────";
    }


    // [NEW] buildCurrencyMenu()
    // Returns the currency selection list shown when user starts an exchange.
    private String buildCurrencyMenu() {
        return "Select target currency:\n"
                + "1 → USD (US Dollar)\n"
                + "2 → EUR (Euro)\n"
                + "3 → JPY (Japanese Yen)\n"
                + "4 → AED (UAE Dirham)\n"
                + "5 → CAD (Canadian Dollar)\n"
                + "\nEnter number (1-5), then Ent.";
    }


    // GETTERS — Read-only access for View
    // The View calls these in its update() method to refresh the display.
    // All fields are private so these getters are the only access point.

    /** Returns the current status/instruction message (single line) */
    public String getMessage() {
        return message;
    }

    /** Returns the content of the number pad input TextField */
    public String getNumberPadInput() {
        return numberPadInput;
    }

    /** Returns the content of the result TextArea (may be multiline) */
    public String getResult() {
        return result;
    }

    /** Returns the current FSM state string (used by View to decide masking) */
    public String getState() {
        return state;
    }

    /**
     * [NEW] Returns the account type name of the logged-in account.
     * Used by the View to display the account type badge.
     * Returns empty string if not logged in.
     */
    public String getAccountTypeDisplay() {
        if (bank.loggedIn()) {
            BankAccount acc = bank.getLoggedInAccount();
            return acc != null ? acc.getAccountTypeName() : "";
        }
        return "";
    }

    /**
     * [NEW] Returns true if a user is currently logged in.
     * Used by the View to decide whether to show the account type badge.
     */
    public boolean isLoggedIn() {
        return bank.loggedIn();
    }

    /** Returns the STATE_PASSWORD constant — used by View for input masking */
    public String getStatePassword() {
        return STATE_PASSWORD;
    }

    /** Returns the STATE_CH_PASS constant — used by View for input masking */
    public String getStateChPass() {
        return STATE_CH_PASS;
    }
}
