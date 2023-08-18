package com.techelevator.tenmo.model.transfer;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TransferDto {
    private int transferId;
    private int typeId;
    private String type;
    private int transferStatusId;
    private String status;
    private int accountFrom;
    private String fromUsername;
    private int accountTo;
    private String toUsername;
    private BigDecimal amount;
    private Timestamp transferCreated;
    private Timestamp transferCompleted;

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getTransferStatusId() {
        return transferStatusId;
    }

    public void setTransferStatusId(int transferStatusId) {
        this.transferStatusId = transferStatusId;
    }

    public int getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(int accountFrom) {
        this.accountFrom = accountFrom;
    }

    public int getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(int accountTo) {
        this.accountTo = accountTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Timestamp getTransferCreated() {
        return transferCreated;
    }

    public void setTransferCreated(Timestamp transferCreated) {
        this.transferCreated = transferCreated;
    }

    public Timestamp getTransferCompleted() {
        return transferCompleted;
    }

    public void setTransferCompleted(Timestamp transferCompleted) {
        this.transferCompleted = transferCompleted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }
}
