package com.atmbanksimulator;

import java.util.ArrayList;

public class UIModel {
    View view;
    private Bank bank;

    private final String STATE_ACCOUNT_NO = "account_no";
    private final String STATE_PASSWORD = "password";
    private final String STATE_LOGGED_IN = "logged_in";
    private final String STATE_CH_PASS = "change_password";

    private String state = STATE_ACCOUNT_NO;
    private String accNumber = "";
    private String accPasswd = "";
    private String oldPasswd = "";

    private String message;
    private String numberPadInput;
    private String result;

    public UIModel(Bank bank) {
        this.bank = bank;
    }

    public void initialise() {
        setState(STATE_ACCOUNT_NO);
        numberPadInput = "";
        message = "✨ Welcome to Brighton Bank ATM ✨";
        result = "Enter your account number\nFollowed by \"Ent\"";
        update();
    }

    private void reset(String msg) {
        setState(STATE_ACCOUNT_NO);
        numberPadInput = "";
        message = msg;
        result = "Enter your account number\nFollowed by \"Ent\"";
    }

    private void setState(String newState) {
        if (!state.equals(newState)) {
            String oldState = state;
            state = newState;
            System.out.println("UIModel::setState: changed state from " + oldState + " to " + newState);
        }
    }

    public void processNumber(String numberOnButton) {
        if (numberPadInput.length() < 8) {
            numberPadInput += numberOnButton;
            message = "Beep! " + numberOnButton + " received";
            result = "";
        } else {
            message = "Input too long!";
            result = "Maximum amount length reached.";
        }
        update();
    }

    public void processClear() {
        if (!numberPadInput.isEmpty()) {
            numberPadInput = "";
            message = "Input Cleared";
            update();
        }
    }

    public void processEnter() {
        switch (state) {
            case STATE_ACCOUNT_NO:
                if (numberPadInput.equals("")) {
                    message = "Invalid Account Number";
                    reset(message);
                } else {
                    accNumber = numberPadInput;
                    numberPadInput = "";
                    setState(STATE_PASSWORD);
                    message = "Account Number Accepted";
                    result = "Now enter your password\nFollowed by \"Ent\"";
                }
                break;

            case STATE_PASSWORD:
                accPasswd = numberPadInput;
                numberPadInput = "";
                if (bank.login(accNumber, accPasswd)) {
                    setState(STATE_LOGGED_IN);
                    message = "Logged In";
                    result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
                } else {
                    message = "Login failed: Unknown Account/Password";
                    reset(message);
                }
                break;

            case STATE_CH_PASS:
                String newPasswd = numberPadInput;
                if (bank.changePassword(accPasswd, newPasswd)) {
                    accPasswd = newPasswd;
                    message = "Password Changed Successfully";
                    setState(STATE_LOGGED_IN);
                    result = "Main Menu\nChoose an option.";
                } else {
                    message = "Change Failed: min 5 digits";
                    result = "Try again: Enter NEW Password\nFollowed by 'Ent'";
                }
                numberPadInput = "";
                break;

            case STATE_LOGGED_IN:
            default:
        }
        update();
    }

    private int parseValidAmount(String number) {
        if (number.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void processBalance() {
        if (state.equals(STATE_LOGGED_IN)) {
            numberPadInput = "";
            message = "Balance Available";
            result = "Your Balance is: " + bank.getBalance();
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processWithdraw() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);
            if (amount > 0) {
                if (bank.withdraw(amount)) {
                    message = "Withdraw Successful";
                    result = "Withdrawn: " + numberPadInput;
                } else {
                    message = "Withdraw Failed: Insufficient Funds";
                    result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
                }
            } else {
                message = "Invalid Amount";
                result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
            }
            numberPadInput = "";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processDeposit() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);
            if (amount > 0) {
                bank.deposit(amount);
                message = "Deposit Successful";
                result = "Deposited: " + numberPadInput;
            } else {
                message = "Invalid Amount";
                result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
            }
            numberPadInput = "";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processTransfer() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);
            if (amount > 0) {
                String targetAccount = accNumber;
                boolean success = bank.transfer(targetAccount, amount);
                if (success) {
                    message = "Transfer Successful";
                    result = "Transferred: " + numberPadInput;
                } else {
                    message = "Transfer Failed";
                    result = "Invalid target or insufficient funds";
                }
            } else {
                message = "Invalid Amount";
                result = "Enter amount then press Transfer";
            }
            numberPadInput = "";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    // ===== MINI STATEMENT METHOD =====
    public void processMiniStatement() {
        if (state.equals(STATE_LOGGED_IN)) {
            BankAccount acc = bank.getLoggedInAccount();
            if (acc != null) {
                result = acc.getMiniStatement();
                message = "Mini Statement";
            } else {
                message = "Error";
                result = "Could not retrieve account";
            }
        } else {
            reset("Not logged in");
        }
        update();
    }

    public void processChP() {
        if (state.equals(STATE_LOGGED_IN)) {
            setState(STATE_CH_PASS);
            numberPadInput = "";
            message = "Changing Password";
            result = "Enter NEW Password (min 5 digits)\nFollowed by \"Ent\"";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processFinish() {
        if (state.equals(STATE_LOGGED_IN)) {
            reset("Thank you for using the Bank ATM");
            bank.logout();
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processUnknownKey(String action) {
        reset("Invalid Command");
        update();
    }

    private void update() {
        view.update(message, numberPadInput, result);
    }
}