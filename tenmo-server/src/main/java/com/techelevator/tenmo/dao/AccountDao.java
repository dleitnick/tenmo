package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {

    /**
     * Get an account by it's ID
     * @param id Account ID
     * @return An Account
     */
    Account getAccountById(int id);

    /**
     * Get a list of accounts from a specific user
     * @param user The user of the accounts
     * @return A list of Accounts
     */
    List<Account> getAccounts(User user);

    /**
     * Get the primary account of a user
     * @param user A User
     * @return An Account that is currently the primary account of that user
     */
    Account getPrimaryAccount(User user);

    /**
     * Add to an Account's balance
     * @param account An Account
     * @param moneyToAdd The amount to add
     * @return The Account with updated balance
     */
    Account addToBalance(Account account, BigDecimal moneyToAdd);

    /**
     * Subtract from an Account's balance
     * @param account An Account
     * @param moneyToSubtract The amount to subtract
     * @return The Account with updated balance
     */
    Account subtractFromBalance(Account account, BigDecimal moneyToSubtract);

    /**
     * Create a new account
     * @param user The User that the account will be created for
     * @return The new Account
     */
    Account createNewAccount(User user);

    /**
     * Set the primary account of a user
     * @param account The Account that will be primary
     * @return The updated Account set as primary
     */
    Account setPrimaryAccount(Account account);

    /**
     * Get the user of a specific account
     * @param account The Account to lookup
     * @return The User of that account
     */
    User getUserOfAccount(Account account);

    /**
     * Delete an account (Should only add a deleted flag to the account)
     * @param account Account to be deleted
     * @param primaryUser The user that owns that account
     * @return A boolean indicating whether or not the account was deleted
     */
    boolean deleteAccount(Account account, User primaryUser);
}
