package com.techelevator.tenmo.model.transfer;

import com.techelevator.tenmo.services.ConsoleService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class TransferSendDto {

    private ConsoleService consoleService = new ConsoleService();

    private int transferId;
    private int accountId;
    private String senderName;

    private String receiverName;

    private BigDecimal amountSent;
    private BigDecimal balance;
    private Timestamp timeCompleted;

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public BigDecimal getAmountSent() {
        return amountSent;
    }

    public void setAmountSent(BigDecimal amountSent) {
        this.amountSent = amountSent;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Timestamp getTimeCompleted() {
        return timeCompleted;
    }

    public void setTimeCompleted(Timestamp timeCompleted) {
        this.timeCompleted = timeCompleted;
    }

    @Override
    public String toString() {
        DateTimeFormatter dTFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        StringBuilder str = new StringBuilder();
        String title = "Transfer Sent\n";
        String idStr = String.format("%10s %s\n", "Id:", transferId);
        String to = String.format("%10s %s\n", "To:", receiverName);
        String amount = String.format("%10s %s\n", "Amount:", currency.format(amountSent));
        String completed = String.format("%10s %s", "Completed:", timeCompleted.toLocalDateTime().format(dTFormat));
        String[] transferItems = {idStr, to, amount, completed};
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
        str.append(printDivider(longestLength));
        str.append(title);
        str.append(printDivider(longestLength));
        str.append(idStr);
        str.append(to);
        str.append(amount);
        str.append(completed);
        return str.toString();
    }

    public String printDivider(int dividerLength) {
        StringBuilder str = new StringBuilder();
        str.append("-".repeat(Math.max(0, dividerLength))).append("\n");
        return str.toString();
    }
}
