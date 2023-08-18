package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.util.ColorUtil;

import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleService {

    private final Scanner scanner = new Scanner(System.in);
    private ColorUtil banner = new ColorUtil();
    private ColorUtil header = new ColorUtil(5, 0);
    private ColorUtil subHeader = new ColorUtil(5, 0, false);

    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to Tenmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");
        System.out.println("6: Create or change accounts");
        System.out.println("0: Logout");
        System.out.println();
    }

    public void printApproveOrRejectMenu() {
        System.out.println();
        System.out.println("1: Approve");
        System.out.println("2: Reject");
        System.out.println("0: Don't approve or reject");
        System.out.println();
    }

    public void printAccountsMenu() {
        System.out.println("1. View Accounts");
        System.out.println("2. Create Account");
        System.out.println("3. Delete Account");
        System.out.println("4. Move funds between accounts");
        System.out.println("0. Exit");
    }

    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username.toLowerCase(), password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                BigDecimal value = new BigDecimal(scanner.nextLine());
                if (value.compareTo(BigDecimal.ZERO) < 1) {
                    throw new NumberFormatException("Amount must be greater than 0.");
                }
                return value;

            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printDivider(int dividerLength) {
        for (int i = 0; i < dividerLength; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    public void printBanner(String message) {
        System.out.println();
        banner.print(message);
    }

    public void printHeader(String message) {
        System.out.println();
        header.print(message);
    }

    public void printSubHeader(String message) {
        System.out.println();
        subHeader.print(message);
    }


}
