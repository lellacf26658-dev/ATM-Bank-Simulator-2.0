package com.atmbanksimulator;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String args[]) {
        launch(args);
    }

    public void start(Stage window) {
        Bank bank = DataManager.loadBank();

        if (bank.getNumAccounts() == 0) {
            bank.addBankAccount("10001", "11111", 100, Bank.AccountType.STANDARD);
            bank.addBankAccount("10002", "22222", 50, Bank.AccountType.STUDENT);
            bank.addBankAccount("10003", "33333", 1000, Bank.AccountType.PRIME);
            bank.addBankAccount("10004", "44444", 500, Bank.AccountType.SAVING);
        }

        UIModel UIModel = new UIModel(bank);
        View view = new View();
        Controller controller = new Controller();

        view.controller = controller;
        controller.UIModel = UIModel;
        UIModel.view = view;

        window.setOnCloseRequest(e -> {
            DataManager.saveBank(bank);
            System.out.println("Bank data saved on exit");
        });

        view.start(window);
        UIModel.initialise();
    }
}