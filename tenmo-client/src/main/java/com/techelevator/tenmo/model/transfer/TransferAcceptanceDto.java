package com.techelevator.tenmo.model.transfer;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TransferAcceptanceDto {
    private boolean transferAccepted;
    private int transferId;
    private int accountId;
    private String senderName;
    private String receiverName;
    private BigDecimal amountSent;
    private BigDecimal balance;
    private Timestamp timeCompleted;

    public boolean isTransferAccepted() {
        return transferAccepted;
    }

    public void setTransferAccepted(boolean transferAccepted) {
        this.transferAccepted = transferAccepted;
    }

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
}
