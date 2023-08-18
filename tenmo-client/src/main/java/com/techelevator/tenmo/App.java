package com.techelevator.tenmo;

import com.techelevator.exception.ApiException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.model.transfer.TransferAcceptanceDto;
import com.techelevator.tenmo.model.transfer.TransferDto;
import com.techelevator.tenmo.model.transfer.TransferRequestDto;
import com.techelevator.tenmo.model.transfer.TransferSendDto;
import com.techelevator.tenmo.services.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    public static final int EXIT_APP = 0;
    public static final int VIEW_CURRENT_BALANCE = 1;
    public static final int VIEW_PAST_TRANSFERS = 2;
    public static final int VIEW_PENDING_REQUESTS = 3;
    public static final int SEND_TE_BUCKS = 4;
    public static final int REQUEST_TE_BUCKS = 5;
    public static final int CREATE_OR_CHANGE_ACCOUNT = 6;
    public static final int REGISTER_USER = 1;
    public static final int LOGIN_USER = 2;
    private final DateTimeFormatter dTFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
    private final DateTimeFormatter dTFormatMedium = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private AccountService accountService;
    private TransferService transferService;
    private UserService userService;
    private AuthenticatedUser currentUser;
    private NumberFormat currency = NumberFormat.getCurrencyInstance();

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            menuSelection = loginMenu();
            if (currentUser != null) {
                // TODO: Instantiate services that require the current user to exist here
                accountService = new AccountService(API_BASE_URL, currentUser.getToken());
                transferService = new TransferService(API_BASE_URL, currentUser.getToken());
                userService = new UserService(API_BASE_URL, currentUser.getToken());
                mainMenu();
            }
        }
    }
    private int loginMenu() {
        consoleService.printHeader("Tenmo: Money Transfer Service\nTransfer and request money from other users\nWe put the 'TE' n' Mo");
        int menuSelection = -1;
        while (menuSelection != EXIT_APP && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == REGISTER_USER) {
                handleRegister();
            } else if (menuSelection == LOGIN_USER) {
                handleLogin();
            } else if (menuSelection != EXIT_APP) {
                consoleService.printMessage("Invalid Selection");
                consoleService.pause();
            }
        }
        return menuSelection;
    }

    private void handleRegister() {
        consoleService.printMessage("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            consoleService.printMessage("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            consoleService.printHeader("Tenmo");
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == VIEW_CURRENT_BALANCE) {
                viewCurrentBalance();
            } else if (menuSelection == VIEW_PAST_TRANSFERS) {
                viewTransferHistory();
            } else if (menuSelection == VIEW_PENDING_REQUESTS) {
                viewPendingRequests();
            } else if (menuSelection == SEND_TE_BUCKS) {
                sendBucks();
            } else if (menuSelection == REQUEST_TE_BUCKS) {
                requestBucks();
            } else if (menuSelection == CREATE_OR_CHANGE_ACCOUNT) {
                handleAccounts();
            } else if (menuSelection == EXIT_APP) {
                currentUser = null;
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void handleAccounts() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            String subHeader = "Accounts";
            consoleService.printSubHeader(subHeader);
            consoleService.printDivider(subHeader.length() + 16);
            consoleService.printAccountsMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewAccounts();
            } else if (menuSelection == 2) {
                createAccount();
            } else if (menuSelection == 3) {
                deleteAccount();
            } else if (menuSelection == 4) {
                moveBetweenAccounts();
            } else if (menuSelection == EXIT_APP) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
        }
    }

    private void viewAccounts() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            Account[] accounts = accountService.getAllAccountsBalance();
            Account primaryAccount = accountService.getPrimaryAccountBalance();
            String columnFormat = "%-8s%-8s%13s";
            String columnNames = String.format(columnFormat, "ID", "Active", "Balance");
            String subHeader = "Your Accounts";
            int subHeaderSpacing = (columnNames.length() - subHeader.length() - 16) / 2;
            String subHeaderFormat = "%-" + subHeaderSpacing + "s%s%-" + subHeaderSpacing + "s";
            consoleService.printSubHeader(subHeader);
            consoleService.printDivider(columnNames.length());
            System.out.println(columnNames);
            consoleService.printDivider(columnNames.length());
            BigDecimal totalBalance = BigDecimal.ZERO;
            for (Account account : accounts) {
                int id = account.getAccountId();
                BigDecimal balance = account.getAccountBalance();
                totalBalance = totalBalance.add(balance);
                boolean isActive = account.equals(primaryAccount);
                String columns = String.format(columnFormat, id, isActive, currency.format(balance));
                consoleService.printMessage(columns);

            }
            consoleService.printDivider(columnNames.length());
            String totalLineFormat = "%" + columnNames.length() + "s";
            String total = "Total: " + currency.format(totalBalance);
            String totalLine = String.format(totalLineFormat, total);
            consoleService.printMessage(totalLine);
            consoleService.printDivider(columnNames.length());
            menuSelection = consoleService.promptForMenuSelection("Change your active account by selecting it's ID (0 to cancel): ");
            if (menuSelection > 0 ) {
                Account newPrimary = accountService.setPrimaryAccount(menuSelection);
                if (newPrimary == null) {
                    consoleService.printMessage("Please enter a valid account ID.");
                    continue;
                } else {
                    consoleService.printMessage("Active account changed to " + newPrimary.getAccountId() + "!");
                }
            } else if (menuSelection == EXIT_APP) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void moveBetweenAccounts() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            Account[] accounts = accountService.getAllAccountsBalance();
            Account primaryAccount = accountService.getPrimaryAccountBalance();
            String columnFormat = "%-8s%-8s%13s";
            String columnNames = String.format(columnFormat, "ID", "Active", "Balance");
            String subHeader = "Move funds between Account";
            int subHeaderSpacing = (columnNames.length() - subHeader.length() - 16) / 2;
            String subHeaderFormat = "%-" + subHeaderSpacing + "s%s%-" + subHeaderSpacing + "s";
            consoleService.printSubHeader(subHeader);
            consoleService.printDivider(columnNames.length());
            System.out.println(columnNames);
            consoleService.printDivider(columnNames.length());
            for (Account account : accounts) {
                int id = account.getAccountId();
                BigDecimal balance = account.getAccountBalance();
                boolean isActive = account.equals(primaryAccount);
                String columns = String.format(columnFormat, id, isActive, currency.format(balance));
                consoleService.printMessage(columns);
            }
            consoleService.printDivider(columnNames.length());
            menuSelection = consoleService.promptForMenuSelection("Select an account to move funds to (0 to cancel): ");
            if (menuSelection > 0 && isUsersAccount(menuSelection, accounts)) {
                BigDecimal amount = consoleService.promptForBigDecimal("Enter an amount to move: ");
                TransferSendDto transferSendDto = null;
                try {
                    transferSendDto = transferService.moveBetweenAccounts(currentUser.getUser().getUsername(), amount, menuSelection);
                    consoleService.printMessage(transferSendDto.toString());
                } catch (ApiException e) {
                    consoleService.printMessage(e.getLocalizedMessage());
                }
            } else if (menuSelection == EXIT_APP) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private boolean isUsersAccount(int accountId, Account[] accounts) {
        for (Account account : accounts) {
            if (accountId == account.getAccountId() && !account.isPrimary()) {
                return true;
            }
        }
        return false;
    }

    private void deleteAccount() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            Account[] accounts = accountService.getAllAccountsBalance();
            Account primaryAccount = accountService.getPrimaryAccountBalance();
            String columnFormat = "%-8s%-8s%13s";
            String columnNames = String.format(columnFormat, "ID", "Active", "Balance");
            String subHeader = "Delete Account";
            int subHeaderSpacing = (columnNames.length() - subHeader.length() - 16) / 2;
            String subHeaderFormat = "%-" + subHeaderSpacing + "s%s%-" + subHeaderSpacing + "s";
            consoleService.printSubHeader(subHeader);
            consoleService.printDivider(columnNames.length());
            System.out.println(columnNames);
            consoleService.printDivider(columnNames.length());
            for (Account account : accounts) {
                int id = account.getAccountId();
                BigDecimal balance = account.getAccountBalance();
                boolean isActive = account.equals(primaryAccount);
                String columns = String.format(columnFormat, id, isActive, currency.format(balance));
                consoleService.printMessage(columns);
            }
            consoleService.printDivider(columnNames.length());
            menuSelection = consoleService.promptForMenuSelection("Deleting an account will move its funds to your active account.\n" +
                    "Delete your account by selecting it's ID (0 to cancel): ");
            if (menuSelection > 0 ) {
                boolean success = accountService.deleteAccount(menuSelection);
                if (!success) {
                    consoleService.printMessage("Please enter a valid account ID. (You cannot delete your active account.)");
                    continue;
                } else {
                    consoleService.printMessage("Account deleted!");
                }
            } else if (menuSelection == EXIT_APP) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void createAccount() {
        Account primaryAccount = accountService.getPrimaryAccountBalance();
        String subHeader = "Create Account";
        consoleService.printSubHeader(subHeader);
        consoleService.printDivider(subHeader.length() + 16);
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            menuSelection = consoleService.promptForMenuSelection("Enter 1 to create new account (0 to cancel): ");
            if (menuSelection == 1 ) {
                Account createdAccount = accountService.createNewAccount();
                consoleService.printMessage("Account ID: " + createdAccount.getAccountId() + " created!");
                menuSelection = 0;
            } else if (menuSelection == EXIT_APP) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        Account primaryAccount = accountService.getPrimaryAccountBalance();
        BigDecimal primaryAccountBalance = primaryAccount.getAccountBalance();
        String balance = String.format("Your current balance is: %s.", currency.format(primaryAccountBalance));
        String subHeader = "Balance (account ID: " + primaryAccount.getAccountId() + ")";
        int subHeaderSpacing = (balance.length() - subHeader.length() - 16) / 2;
        String subHeaderFormat;
        if (subHeaderSpacing > 0) {
            subHeaderFormat = "%-" + subHeaderSpacing + "s%s%-" + subHeaderSpacing + "s";
            consoleService.printSubHeader(String.format(subHeaderFormat, " ", subHeader, " "));
            consoleService.printDivider(subHeader.length() + (2 * subHeaderSpacing));
        } else {
            consoleService.printSubHeader(subHeader);
            consoleService.printDivider(subHeader.length() + 16);
        }
        consoleService.printMessage(balance);
    }

    private void viewTransferHistory() {
        TransferDto[] allTransfers = transferService.getAllTransfers();
        String columnNames = String.format("%-8s%-20s%-10s%10s", "ID", "From/To", "Status", "Amount");
        if (allTransfers != null) {
            String subHeader = "Transfer History";
            int subHeaderSpacing = (columnNames.length() - subHeader.length() - 16) / 2;
            String subHeaderFormat = "%-" + subHeaderSpacing + "s%s%-" + subHeaderSpacing + "s";
            consoleService.printSubHeader(String.format(subHeaderFormat, " ", subHeader, " "));
            consoleService.printDivider(columnNames.length());
            System.out.println(columnNames);
            consoleService.printDivider(columnNames.length());
            BigDecimal totalBalance = BigDecimal.ZERO;
            for (TransferDto transfer : allTransfers) {
                int id = transfer.getTransferId();
                String status = transfer.getStatus();
                String fromOrTo;
                BigDecimal amount = transfer.getAmount();
                if (transfer.getFromUsername().equals(transfer.getToUsername())) {
                    fromOrTo = String.format("%-6s", "To/from yourself");
                    if (transfer.getAccountFrom() == accountService.getPrimaryAccountBalance().getAccountId()) {
                        if (transfer.getStatus().equals("Approved")) {
                            totalBalance = totalBalance.subtract(amount);
                        }
                    } else {
                        if (transfer.getStatus().equals("Approved")) {
                            totalBalance = totalBalance.add(amount);
                        }
                    }
                } else if (transfer.getFromUsername().equals(currentUser.getUser().getUsername())) {
                    fromOrTo = String.format("%-6s%s", "To:", transfer.getToUsername());
                    if (transfer.getStatus().equals("Approved")) {
                        totalBalance = totalBalance.subtract(amount);
                    }
                } else {
                    fromOrTo = String.format("%-6s%s", "From:", transfer.getFromUsername());
                    if (transfer.getStatus().equals("Approved")) {
                        totalBalance = totalBalance.add(amount);
                    }
                }
                String columns = String.format("%-8s%-20s%-10s%10s", id, fromOrTo, status, currency.format(amount));
                consoleService.printMessage(columns);
            }
            consoleService.printDivider(columnNames.length());
            String totalLineFormat = "%" + columnNames.length() + "s";
            String total = "Total: " + currency.format(totalBalance);
            String totalLine = String.format(totalLineFormat, total);
            consoleService.printMessage(totalLine);
            consoleService.printDivider(columnNames.length());
            int menuSelection = -1;
            while (menuSelection != EXIT_APP) {
                menuSelection = consoleService.promptForMenuSelection("Please enter transfer ID to view details (0 to cancel): ");
                if (menuSelection > 0 ) {
                    viewTransfer(menuSelection);
                } else if (menuSelection == EXIT_APP) {
                    continue;
                } else {
                    consoleService.printMessage("Invalid Selection");
                }
                consoleService.pause();
            }
        } else {
            consoleService.printMessage("You have no transfers.");
        }

    }

    private void viewPendingRequests() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            TransferDto[] allTransfers = transferService.getPendingTransfers();
            List<TransferDto> usersPendingTransfers = new ArrayList<>();
            if (allTransfers != null) {
                for (TransferDto transfer : allTransfers) {
                    if (transfer.getFromUsername().equals(currentUser.getUser().getUsername())) {
                        usersPendingTransfers.add(transfer);
                    }
                }
            }
            String columnNames = String.format("%-8s%-20s%10s", "ID", "To", "Amount");
            if (usersPendingTransfers.size() > 0) {
                String subHeader = "Pending Requests";
                int subHeaderSpacing = (columnNames.length() - subHeader.length() - 16) / 2;
                String subHeaderFormat = "%-" + subHeaderSpacing + "s%s%-" + subHeaderSpacing + "s";
                consoleService.printSubHeader(String.format(subHeaderFormat, " ", subHeader, " "));
                consoleService.printDivider(columnNames.length());
                System.out.println(columnNames);
                consoleService.printDivider(columnNames.length());
                for (TransferDto transfer : usersPendingTransfers) {
                    int id = transfer.getTransferId();
                    String fromOrTo = transfer.getToUsername();
                    BigDecimal amount = transfer.getAmount();
                    String columns = String.format("%-8s%-20s%10s", id, fromOrTo, currency.format(amount));
                    consoleService.printMessage(columns);
                }
                consoleService.printDivider(columnNames.length());
                menuSelection = consoleService.promptForMenuSelection("Please enter transfer ID to approve/reject (0 to cancel): ");
                if (menuSelection > 0 ) {
                    approveOrReject(menuSelection);
                } else if (menuSelection == EXIT_APP) {
                    continue;
                } else {
                    consoleService.printMessage("Invalid Selection");
                }
                consoleService.pause();
            } else {
                consoleService.printMessage("There are no pending requests.");
                break;
            }
        }
    }

    private void approveOrReject(int id) {
        viewTransfer(id);
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            consoleService.printApproveOrRejectMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1 ) {
                TransferAcceptanceDto transfer = null;
                try {
                    transfer = transferService.approveOrReject(id, true);
                    if (transfer.getTimeCompleted() != null) {
                        consoleService.printMessage("Transfer was approved!");
                    }
                } catch (ApiException e) {
                    consoleService.printMessage("You don't have enough funds in this account to approve this transfer.");
                }
                break;
            } else if (menuSelection == 2 ) {
                TransferAcceptanceDto transfer = transferService.approveOrReject(id, false);
                if (transfer.getTimeCompleted() != null) {
                    consoleService.printMessage("Transfer was rejected!");
                }
                break;
            } else if (menuSelection == EXIT_APP) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }

    }

    private void sendBucks() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            User[] allUsers = displayUsers();
            menuSelection = consoleService.promptForMenuSelection("Enter ID of user you are sending to (0 to cancel): ");
            if (menuSelection > 0 && menuSelection <= allUsers.length) {
                BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
                TransferSendDto transferSendDto = null;
                try {
                    transferSendDto = transferService.send(allUsers[menuSelection - 1].getUsername(), amount);
                    consoleService.printMessage(transferSendDto.toString());
                } catch (ApiException e) {
                    consoleService.printMessage(e.getLocalizedMessage());
                }
            } else if (menuSelection == EXIT_APP) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void requestBucks() {
        int menuSelection = -1;
        while (menuSelection != EXIT_APP) {
            User[] allUsers = displayUsers();
            menuSelection = consoleService.promptForMenuSelection("Enter ID of user you are requesting from (0 to cancel): ");
            if (menuSelection > 0 && menuSelection <= allUsers.length) {
                BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
                TransferRequestDto transferRequestDto = transferService.request(allUsers[menuSelection - 1].getUsername(), amount);
                consoleService.printMessage(transferRequestDto.toString());
            } else if (menuSelection == EXIT_APP) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private User[] displayUsers() {
        User[] allUsers = userService.getAllUsers();
        String columnNames = String.format("%-8s%-20s", "ID", "Name");
        String subHeader = "Users";
        int subHeaderSpacing = (columnNames.length() - subHeader.length() - 16) / 2;
        String subHeaderFormat = "%-" + subHeaderSpacing + "s%s%-" + subHeaderSpacing + "s";
        consoleService.printSubHeader(String.format(subHeaderFormat, " ", subHeader, " "));
        consoleService.printDivider(columnNames.length());
        System.out.println(columnNames);
        consoleService.printDivider(columnNames.length());
        for (int i = 0; i < allUsers.length; i++) {
            int id = i + 1;
            String name = allUsers[i].getUsername();
            String columns = String.format("%-8s%-20s", id, name);
            consoleService.printMessage(columns);
        }
        consoleService.printDivider(columnNames.length());
        return allUsers;
    }

    private void viewTransfer(int id) {
        TransferDto transfer = transferService.getTransfer(id);
        if (transfer != null) {
            String title = "Transfer Details";
            String idStr = String.format("%10s %s", "Id:", transfer.getTransferId());
            String from = String.format("%10s %s", "From:", transfer.getFromUsername());
            String to = String.format("%10s %s", "To:", transfer.getToUsername());
            String type = String.format("%10s %s", "Type:", transfer.getType());
            String status = String.format("%10s %s", "Status:", transfer.getStatus());
            String amount = String.format("%10s %s", "Amount:", currency.format(transfer.getAmount()));
            String created = String.format("%10s %s", "Created:", transfer.getTransferCreated().toLocalDateTime().format(dTFormatMedium));
            String completed = "";
            if (transfer.getTransferCompleted() != null) {
                completed = String.format("%10s %s", "Completed:", transfer.getTransferCompleted().toLocalDateTime().format(dTFormatMedium));
            }
            String[] transferItems = {idStr, from, to, type, status, amount, created, completed};
            int longestLength = 0;
            for (String transferItem : transferItems) {
                if (transferItem.length() > longestLength) {
                    longestLength = transferItem.length();
                }
            }
            if (title.length() > longestLength) longestLength = title.length();
            int lengthOfTitle = title.length();
            int halfway = longestLength - lengthOfTitle;
            String gap = "%" + halfway / 2 + "s";
            title = String.format(gap + "%s", "", title);
            consoleService.printDivider(longestLength);
            consoleService.printMessage(title);
            consoleService.printDivider(longestLength);
            consoleService.printMessage(idStr);
            consoleService.printMessage(from);
            consoleService.printMessage(to);
            consoleService.printMessage(type);
            consoleService.printMessage(status);
            consoleService.printMessage(amount);
            consoleService.printMessage(created);
            if (!completed.equals("")) {
                consoleService.printMessage(completed);
            }
        } else consoleService.printMessage("No transfer found with id: " + id);
    }

}
