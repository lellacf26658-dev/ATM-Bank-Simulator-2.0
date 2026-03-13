package com.atmbanksimulator;

import javafx.application.Application;
import javafx.stage.Stage;

// MVC Structure Analogy:
// - View is the face and senses: it shows things and receives input.
// - Controller is the nerves: it carries signals to the brain and triggers actions.
// - UIModel is the brain: it holds state and logic, and queries domain services.
// - Bank / BankAccount are the real "money world" rules.
// Together, they simulate how an ATM thinks, reacts, and handles money.

public class Main extends Application {
    public static void main(String args[]) {
        launch(args);
    }

    public void start(Stage window) {
        // Create a Bank object
        Bank bank = new Bank();

        // Add test accounts with different types
        bank.addBankAccount("10001", "11111", 100, Bank.AccountType.STANDARD);
        bank.addBankAccount("10002", "22222", 50, Bank.AccountType.STUDENT);
        bank.addBankAccount("10003", "33333", 1000, Bank.AccountType.PRIME);
        bank.addBankAccount("10004", "44444", 500, Bank.AccountType.SAVING);

        // Setup MVC structure
        UIModel UIModel = new UIModel(bank);
        View view = new View();
        Controller controller = new Controller();

        // Link MVC components together
        view.controller = controller;
        controller.UIModel = UIModel;
        UIModel.view = view;

        // Start the GUI and initialize the UIModel
        view.start(window);
        UIModel.initialise();
    }
}