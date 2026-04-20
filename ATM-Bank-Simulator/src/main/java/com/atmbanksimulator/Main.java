package com.atmbanksimulator;

// Main (Application Entry Point)

// CHANGES FROM ORIGINAL:
// 1. [NEW] More test accounts added — one of each type (Standard, Student, Prime, Saving)
//    plus extra accounts for testing the Transfer feature.
// 2. [KEEP] MVC wiring unchanged — view.controller, controller.UIModel, UIModel.view
// 3. [NEW] A comment block explaining how to log into each test account for demos.

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) {

        // Step 1: Create the Bank (Domain layer)
        // The Bank holds all BankAccount objects and manages login/transfer.
        Bank bank = new Bank();

        // Step 2: Add test accounts
        // Format: addBankAccount(accountNumber, password, startingBalance, accountType)
        //
        // ┌──────────┬──────────┬─────────┬──────────┬─────────────────────────────────┐
        // │ Acc No.  │ Password │ Balance │ Type     │ Special Feature                 │
        // ├──────────┼──────────┼─────────┼──────────┼─────────────────────────────────┤
        // │ 10001    │ 11111    │ £100    │ STANDARD │ Standard account, no extras     │
        // │ 10002    │ 22222    │ £50     │ STUDENT  │ £50 daily withdrawal limit      │
        // │ 10003    │ 33333    │ £1000   │ PRIME    │ £100 overdraft allowance        │
        // │ 10004    │ 44444    │ £500    │ SAVING   │ 2% interest, press INT to apply │
        // │ 10005    │ 55555    │ £200    │ STANDARD │ Extra account for Transfer tests│
        // │ 10006    │ 66666    │ £750    │ PRIME    │ Extra Prime for overdraft demos │
        // └──────────┴──────────┴─────────┴──────────┴─────────────────────────────────┘

        bank.addBankAccount("10001", "11111", 100,  Bank.AccountType.STANDARD);
        bank.addBankAccount("10002", "22222", 50,   Bank.AccountType.STUDENT);
        bank.addBankAccount("10003", "33333", 1000, Bank.AccountType.PRIME);
        bank.addBankAccount("10004", "44444", 500,  Bank.AccountType.SAVING);
        bank.addBankAccount("10005", "55555", 200,  Bank.AccountType.STANDARD); // [NEW] Transfer target
        bank.addBankAccount("10006", "66666", 750,  Bank.AccountType.PRIME);    // [NEW] Extra Prime

        // Step 3: Create MVC components
        UIModel uiModel    = new UIModel(bank);
        View    view       = new View();
        Controller controller = new Controller();

        // Step 4: Wire MVC components together
        // Each component holds a reference to the one it communicates with.
        // View     → Controller  (button presses go to controller.process())
        // Controller → UIModel   (controller delegates to uiModel methods)
        // UIModel  → View        (uiModel calls view.update() after state changes)
        view.controller = controller;
        controller.UIModel = uiModel;
        uiModel.view = view;

        // Step 5: Give View a reference to UIModel for reading display data
        // [NEW] The View now reads message/input/result via UIModel getters.
        view.uiModel = uiModel;

        // Step 6: Start the GUI and initialise the UIModel
        // view.start() builds the JavaFX scene and shows the window.
        // uiModel.initialise() sets the ATM to its starting state and
        // calls view.update() for the first time.
        view.start(window);
        uiModel.initialise();
    }
}
