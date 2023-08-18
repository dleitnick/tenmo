package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class BalanceDto {

    private int accountId;
    private BigDecimal accountBalance;
    private boolean isPrimary;

    public BalanceDto(Account account) {
        this.accountId = account.getAccountId();
        this.accountBalance = account.getBalance();
        this.isPrimary = account.isPrimary();
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
}
