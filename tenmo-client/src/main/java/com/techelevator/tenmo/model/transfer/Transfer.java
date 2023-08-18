package com.techelevator.tenmo.model.transfer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

public class Transfer {
    private int transferId;
    private int typeId;
    private int transferStatusId;
    private int accountFrom;
    private int accountTo;
    private BigDecimal amount;
    private Timestamp transferCreated;
    private Timestamp transferCompleted;

    public Transfer(int typeId, int transferStatusId, int accountFrom, int accountTo, BigDecimal amount) {
        this.typeId = typeId;
        this.transferStatusId = transferStatusId;
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.amount = amount;
    }

    public Transfer() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return transferId == transfer.transferId &&
                typeId == transfer.typeId &&
                transferStatusId == transfer.transferStatusId &&
                accountFrom == transfer.accountFrom &&
                accountTo == transfer.accountTo &&
                Objects.equals(amount, transfer.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId, typeId, transferStatusId, accountFrom, accountTo, amount);
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "transferId=" + transferId +
                ", typeId=" + typeId +
                ", transferStatusId=" + transferStatusId +
                ", accountFrom=" + accountFrom +
                ", accountTo=" + accountTo +
                ", amount=" + amount +
                ", transferCreated=" + transferCreated +
                ", transferCompleted=" + transferCompleted +
                '}';
    }
}
