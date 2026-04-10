package com.atmbanksimulator;

import java.io.*;

public class DataManager {
    private static final String FILE_NAME = "bank_data.ser";

    public static void saveBank(Bank bank) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(bank);
            System.out.println("Bank data saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving bank data: " + e.getMessage());
        }
    }

    public static Bank loadBank() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No save file found. Creating new bank.");
            return new Bank();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (Bank) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading bank data: " + e.getMessage());
            return new Bank();
        }
    }
}