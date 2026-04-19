package com.atmbanksimulator;

// Launcher
// Entry point that launches the JavaFX Application.
// This class exists because some IDE/build setups require a non-JavaFX
// main class to avoid module issues when launching.

public class Launcher {
    public static void main(String[] args) {
        javafx.application.Application.launch(Main.class, args);
    }
}
