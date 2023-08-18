package com.techelevator.tenmo.model;
import java.math.BigDecimal;
import java.util.Objects;

public class Account {
    private int accountId;
    private int userId;
    private BigDecimal accountBalance;
    private boolean isPrimary;

    public Account(int accountId, int userId, BigDecimal balance, boolean isPrimary) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountBalance = balance;
        this.isPrimary = isPrimary;
    }

    public Account() {
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountId == account.accountId &&
                userId == account.userId &&
                Objects.equals(accountBalance, account.accountBalance) &&
                isPrimary == account.isPrimary;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, userId, accountBalance, isPrimary);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", userId=" + userId +
                ", balance=" + accountBalance +
                ", isPrimary=" + isPrimary +
                '}';
    }
}