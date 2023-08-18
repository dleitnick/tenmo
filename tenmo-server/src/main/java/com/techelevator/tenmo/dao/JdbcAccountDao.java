package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.exception.InsufficientFundsException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public Account getAccountById(int id) {
        Account account = null;
        String sql = "SELECT * FROM account WHERE account_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            if (results.next()) {
                account = mapRowToAccount(results);
            } else throw new DaoException("No account with ID: " + id);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return account;
    }

    @Override
    public List<Account> getAccounts(User user) {
        List<Account> accountList = new ArrayList<>();
        String sql = "SELECT * FROM account WHERE user_id = ? AND is_deleted = false ORDER BY account_id";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user.getId());
            while (results.next()) {
                accountList.add(mapRowToAccount(results));
            }
            if (accountList.size() == 0) {
                throw new DaoException("User does not exist");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return accountList;
    }

    @Override
    public Account getPrimaryAccount(User user) {
        Account primaryAccount = new Account();
        String sql = "SELECT * FROM account WHERE user_id = ? AND is_primary = true AND is_deleted = false;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user.getId());
            if (results.next()) {
                primaryAccount = (mapRowToAccount(results));
            } else throw new DaoException("Current account is not the Primary Account");
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return primaryAccount;
    }


    @Override
    public Account addToBalance(Account account, BigDecimal moneyToAdd) {
        BigDecimal currentBalance = getAccountById(account.getAccountId()).getBalance();
        if (moneyToAdd.compareTo(new BigDecimal("0.00")) > 0) {
            account.setBalance(currentBalance.add(moneyToAdd));
            String sql = "UPDATE account SET balance=? WHERE account_id=?;";
            jdbcTemplate.update(sql, account.getBalance(), account.getAccountId());
        } else throw new IllegalArgumentException("The money to add should be positive.");
        return account;
    }

    @Override
    public Account subtractFromBalance(Account account, BigDecimal moneyToSubtract) {
        BigDecimal currentBalance = getAccountById(account.getAccountId()).getBalance();
        BigDecimal potentialBalance = currentBalance.subtract(moneyToSubtract);
        if (moneyToSubtract.compareTo(new BigDecimal("0.00")) > 0 && potentialBalance.compareTo(new BigDecimal("0.00")) > -1) {
            account.setBalance(potentialBalance);
            String sql = "UPDATE account SET balance=? WHERE account_id=?;";
            jdbcTemplate.update(sql, account.getBalance(), account.getAccountId());
        } else if (potentialBalance.compareTo(new BigDecimal("0.00")) > 0) {
            throw new IllegalArgumentException("The money to subtract should be positive.");
        } else throw new InsufficientFundsException("The transaction would cause a negative balance.");
        return account;
    }

    @Override
    public Account createNewAccount(User user) {
        Integer newAccountId;
        Account newAccount = null;
        String sql = "INSERT INTO account (user_id, balance, is_primary, is_deleted)\n" +
                "VALUES (?, 0.00, false, false) RETURNING account_id;";
        try {
            newAccountId = jdbcTemplate.queryForObject(sql, Integer.class, user.getId());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        if (newAccountId != null) {
            newAccount = getAccountById(newAccountId);
        }
        return newAccount;
    }

    @Override
    public Account setPrimaryAccount(Account account) {
        String sql = "BEGIN; UPDATE account SET is_primary = false WHERE user_id = ?;" +
                "UPDATE account SET is_primary = true WHERE account_id = ? AND is_deleted = false; COMMIT;";
        try {
            jdbcTemplate.update(sql, account.getUserId(), account.getAccountId());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return getAccountById(account.getAccountId());
    }

    @Override
    public User getUserOfAccount(Account account) {
        User user = null;
        String sql = "SELECT u.username\n" +
                "FROM account a\n" +
                "JOIN tenmo_user u ON u.user_id = a.user_id\n" +
                "WHERE a.account_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, account.getAccountId());
            if (results.next()) {
               user = new User();
               user.setUsername(results.getString("username"));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return user;
    }

    @Override
    public boolean deleteAccount(Account account, User primaryUser) {
        Account primaryAccount = getPrimaryAccount(primaryUser);
        String sql = "BEGIN;\n" +
                "UPDATE account \n" +
                "SET balance = (SELECT balance FROM account WHERE account_id = ?) + \n" +
                "(SELECT balance FROM account WHERE account_id = ?)\n" +
                "WHERE account_id = ?;\n" +
                "UPDATE account\n" +
                "SET balance = 0, is_deleted = true\n" +
                "WHERE account_id = ?;\n" +
                "COMMIT;";
        String sqlTransfer = "INSERT INTO transfer " +
                "(transfer_type_id, transfer_status_id, account_from, account_to, amount, transfer_created, transfer_completed) " +
                "VALUES (3, 2, ?, ?, ?, NOW(), NOW());";
        try {
            jdbcTemplate.update(sql, primaryAccount.getAccountId(), account.getAccountId(), primaryAccount.getAccountId(), account.getAccountId());
            if (account.getBalance().compareTo(new BigDecimal("0.00")) > 0) {
                jdbcTemplate.update(sqlTransfer, account.getAccountId(), primaryAccount.getAccountId(), account.getBalance());
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return true;
    }

    public boolean deleteAccountForTesting(Account account, User primaryUser) {
        Account primaryAccount = getPrimaryAccount(primaryUser);
        String sql =
                "UPDATE account \n" +
                "SET balance = (SELECT balance FROM account WHERE account_id = ?) + \n" +
                "(SELECT balance FROM account WHERE account_id = ?)\n" +
                "WHERE account_id = ?;\n" +
                "UPDATE account\n" +
                "SET balance = 0, is_deleted = true\n" +
                "WHERE account_id = ?";
        String sqlTransfer = "INSERT INTO transfer " +
                "(transfer_type_id, transfer_status_id, account_from, account_to, amount, transfer_created, transfer_completed) " +
                "VALUES (3, 2, ?, ?, ?, NOW(), NOW());";
        try {
            jdbcTemplate.update(sql, primaryAccount.getAccountId(), account.getAccountId(), primaryAccount.getAccountId(), account.getAccountId());
            if (account.getBalance().compareTo(new BigDecimal("0.00")) > 0) {
                jdbcTemplate.update(sqlTransfer, account.getAccountId(), primaryAccount.getAccountId(), account.getBalance());
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return true;
    }

    private Account mapRowToAccount(SqlRowSet results) {
        Account account = new Account();
        account.setAccountId(results.getInt("account_id"));
        account.setUserId(results.getInt("user_id"));
        account.setBalance(results.getBigDecimal("balance"));
        account.setPrimary(results.getBoolean("is_primary"));
        return account;
    }
}
