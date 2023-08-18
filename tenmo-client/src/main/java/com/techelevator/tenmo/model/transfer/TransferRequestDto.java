package com.techelevator.tenmo.model.transfer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class TransferRequestDto {


    private int transferId;
    private int accountId;
    private String payerName;
    private String requesterName;

    private BigDecimal amountRequested;
    private Timestamp timeCreated;

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

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }

    public Timestamp getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Timestamp timeCreated) {
        this.timeCreated = timeCreated;
    }

    @Override
    public String toString() {
        DateTimeFormatter dTFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        StringBuilder str = new StringBuilder();
        String title = "Transfer Requested\n";
        String idStr = String.format("%10s %s\n", "Id:", transferId);
        String from = String.format("%10s %s\n", "From:", payerName);
        String amount = String.format("%10s %s\n", "Amount:", currency.format(amountRequested));
        String created = String.format("%10s %s", "Created:", timeCreated.toLocalDateTime().format(dTFormat));
        String[] transferItems = {idStr, from, amount, created};
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
        str.append(from);
        str.append(amount);
        str.append(created);
        return str.toString();
    }

    public String printDivider(int dividerLength) {
        StringBuilder str = new StringBuilder();
        str.append("-".repeat(Math.max(0, dividerLength))).append("\n");
        return str.toString();
    }
}
