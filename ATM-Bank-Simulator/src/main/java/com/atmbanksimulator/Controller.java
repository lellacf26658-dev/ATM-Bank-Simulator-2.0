package com.atmbanksimulator;

// Controller
// The Controller receives user actions from the View and
// delegates the appropriate tasks to the UIModel.
// Its main job is to decide what to do based on the user input.

// CHANGES FROM ORIGINAL:
// 1. [NEW] case "Stmt" — triggers UIModel.processMiniStmt()
//          Activated when user presses the "STMT" Mini Statement button
// 2. [NEW] case "Cur"  — triggers UIModel.processCurrency()
//          Activated when user presses the "CUR" Currency Exchange button
// 3. [NEW] case "Int"  — triggers UIModel.processInterest()
//          Activated when user presses the "INT" Apply Interest button
//          (only meaningful for SavingAccount holders)

public class Controller {

    // Reference to the UIModel (part of the MVC setup)
    UIModel UIModel;


    // process()
    // Called by the View whenever a button is pressed on the ATM interface.
    // The 'action' String is a short code that identifies which button was pressed.
    // A switch statement routes each action to the correct UIModel method.
    void process(String action) {
        switch (action) {

            // NUMBER PAD BUTTONS
            // All digit buttons (0–9) call processNumber() with the digit as a string.
            // Fall-through is used so all digits share the same handler call.
            case "1": case "2": case "3": case "4": case "5":
            case "6": case "7": case "8": case "9": case "0":
                UIModel.processNumber(action);
                break;

            // CLEAR BUTTON
            // Wipes the current number pad input without changing state.
            case "CLR":
                UIModel.processClear();
                break;

            // ENTER / CONFIRM BUTTON
            // The main confirm button — behaviour depends on current ATM state.
            case "Ent":
                UIModel.processEnter();
                break;

            // WITHDRAW / DEPOSIT SELECTOR
            // "W/D" opens withdraw mode (user types amount then presses this again)
            case "W/D":
                UIModel.processWithdraw();
                break;

            // DEPOSIT BUTTON
            case "Dep":
                UIModel.processDeposit();
                break;

            // BALANCE ENQUIRY
            case "Bal":
                UIModel.processBalance();
                break;

            // TRANSFER — Added by Daniella
            // Starts the two-step transfer flow (target account → amount)
            case "Trf":
                UIModel.processTransfer();
                break;

            // CHANGE PASSWORD — Added by Haaroun
            // Starts the change-password flow (old password → new password)
            case "ChP":
                UIModel.processChP();
                break;

            // [NEW] MINI STATEMENT — Author: Reema
            // Displays the last 5 transactions for the logged-in account.
            // Maps to the "STMT" button in View.java.
            case "Stmt":
                UIModel.processMiniStmt();
                break;

            // [NEW] CURRENCY EXCHANGE — Group feature (IDEAS TO ADD list)
            // Starts the currency conversion flow.
            // Maps to the "CUR" button in View.java.
            case "Cur":
                UIModel.processCurrency();
                break;

            // [NEW] APPLY INTEREST — Author: Daniella (SavingAccount feature)
            // Applies 2% interest to the balance (SavingAccount only).
            // Maps to the "INT" button in View.java.
            case "Int":
                UIModel.processInterest();
                break;

            // FINISH / LOGOUT
            // Logs out the current user and resets the ATM.
            case "Fin":
                UIModel.processFinish();
                break;

            // FALLBACK
            // Any unrecognised action string is reported to the UIModel.
            // Useful during development to catch typos in action codes.
            default:
                UIModel.processUnknownKey(action);
                break;
        }
    }
}
