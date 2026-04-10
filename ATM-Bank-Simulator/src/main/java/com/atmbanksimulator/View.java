package com.atmbanksimulator;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

class View {
    int H = 500;
    int W = 500;

    Controller controller;

    private Label laMsg;
    private TextField tfInput;
    private TextArea taResult;
    private ScrollPane scrollPane;
    private GridPane grid;
    private TilePane buttonPane;

    public void start(Stage window) {
        grid = new GridPane();
        grid.setId("Layout");
        buttonPane = new TilePane();
        buttonPane.setId("Buttons");

        laMsg = new Label("Welcome to Brighton Bank ATM — Please select a transaction");
        laMsg.setFont(new Font("Arial", 22));
        grid.add(laMsg, 0, 0);

        tfInput = new TextField();
        tfInput.setEditable(false);
        tfInput.setPromptText("Enter amount");
        tfInput.setFont(new Font("Consolas", 20));
        tfInput.setStyle("-fx-alignment: center;");
        grid.add(tfInput, 0, 1);

        taResult = new TextArea();
        taResult.setEditable(false);
        taResult.setText("Select a transaction:\nDep = Deposit\nW/D = Withdraw\nBal = Balance\nEnt = Confirm\nCLR = Clear");
        scrollPane = new ScrollPane();
        scrollPane.setContent(taResult);
        grid.add(scrollPane, 0, 2);

        // ===== BUTTON ARRAY WITH "Mini" BUTTON =====
        String buttonTexts[][] = {
                {"7", "8", "9", "", "Dep", ""},
                {"4", "5", "6", "", "W/D", ""},
                {"1", "2", "3", "", "Bal", "Fin"},
                {"CLR", "0", "Mini", "ChP", "", "Ent"}
        };

        for (String[] row : buttonTexts) {
            for (String text : row) {
                if (text.length() >= 1) {
                    Button btn = new Button(text);
                    btn.setOnAction(this::buttonClicked);
                    buttonPane.getChildren().add(btn);
                } else {
                    buttonPane.getChildren().add(new Text());
                }
            }
        }
        grid.add(buttonPane, 0, 3);

        Scene scene = new Scene(grid, W, H);
        scene.getStylesheets().add("atm.css");
        window.setScene(scene);
        window.setTitle("ATM-Bank Simulator");
        window.show();
    }

    private void buttonClicked(ActionEvent event) {
        Button b = ((Button) event.getSource());
        String text = b.getText();
        if (tfInput.getText().length() >= 5 && text.matches("\\d")) {
            taResult.setText("Maximum amount length reached.");
            return;
        }
        controller.process(text);
    }

    public void update(String msg, String tfInputMsg, String taResultMsg) {
        if (msg.contains("Welcome")) {
            return;
        }
        if (msg.contains("GOODBYE") || msg.contains("Thank") || msg.contains("not logged")) {
            showGoodbyeScreen();
            return;
        }
        laMsg.setText(msg);
        tfInput.setText(tfInputMsg);
        taResult.setText(taResultMsg);
    }

    private void showGoodbyeScreen() {
        laMsg.setText("Goodbye! See you later");
        tfInput.clear();
        taResult.setText("Thank you for using HARD ATM.\nHave a nice day.");
    }
}