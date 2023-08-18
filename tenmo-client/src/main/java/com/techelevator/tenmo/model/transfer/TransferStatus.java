package com.techelevator.tenmo.model.transfer;

public class TransferStatus {
    private int transferStatusId;
    private String transferStatusDescription;

    public TransferStatus(int transferStatusId, String transferStatusDescription) {
        this.transferStatusId = transferStatusId;
        this.transferStatusDescription = transferStatusDescription;
    }

    public TransferStatus() {
    }

    public int getTransferStatusId() {
        return transferStatusId;
    }

    public void setTransferStatusId(int transferStatusId) {
        this.transferStatusId = transferStatusId;
    }

    public String getTransferStatusDescription() {
        return transferStatusDescription;
    }

    public void setTransferStatusDescription(String transferStatusDescription) {
        this.transferStatusDescription = transferStatusDescription;
    }
}
