package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.BalanceDto;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private AccountDao accountDao;
    private UserDao userDao;

    public AccountController(AccountDao accountDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    /**
     * Get the balance of the user's primary account
     * @param principal The logged in user
     * @return The balance and account number in the form of a BalanceDto
     */
    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BalanceDto getBalance(Principal principal) {
        BalanceDto balance;
        User user = userDao.getUserByUsername(principal.getName());
        Account account;
        try {
            account = accountDao.getPrimaryAccount(user);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found.");
        }
        return new BalanceDto(account);
    }

    /**
     * Get the balance of all the user's accounts
     * @param principal The logged in user
     * @return The balance and account numbers in the form of BalanceDtos
     */
    @RequestMapping(path = "/balance/all", method = RequestMethod.GET)
    public List<BalanceDto> getBalanceOfAllAccounts(Principal principal) {
        List<BalanceDto> balanceList = new ArrayList<>();
        List<Account> accounts = accountDao.getAccounts(userDao.getUserByUsername(principal.getName()));
        for (Account account : accounts) {
            balanceList.add(new BalanceDto(account));
        }
        return balanceList;
    }

    /**
     * Get the balance of a specified account from the user
     * @param id The account ID
     * @param principal The logged in user
     * @return The balance and account number in the form of a BalanceDto
     */
    @RequestMapping(path = "/balance/{id}", method = RequestMethod.GET)
    public BalanceDto getBalanceByAccountId(@PathVariable int id, Principal principal) {
        BalanceDto balance;
        Account account;
        if (accountDao.getAccountById(id).getUserId() == userDao.getUserByUsername(principal.getName()).getId()) {
            try {
                account = accountDao.getAccountById(id);
            } catch (DaoException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "That account was not found.");
            }
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only get your own balances.");
        return new BalanceDto(account);
    }

    /**
     * Creates a new account for the user
     * @param principal The logged in user
     * @return The account that was created
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/accounts", method = RequestMethod.POST)
    public Account createAccount(Principal principal) {
        Account newAccount;
        try {
            newAccount = accountDao.createNewAccount(userDao.getUserByUsername(principal.getName()));
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No account made.");
        }
        return newAccount;
    }

    /**
     * Deletes an account that is not the user's primary account
     * @param id The account ID to delete
     * @param principal The logged in user
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(path = "/accounts/{id}", method = RequestMethod.DELETE)
    public void deleteAccount(@PathVariable int id, Principal principal) {
        Account accountToDelete = accountDao.getAccountById(id);
        User accountToDeleteUser = userDao.getUserByUsername(accountDao.getUserOfAccount(accountToDelete).getUsername());
        User user = userDao.getUserByUsername(principal.getName());
        if (!accountToDeleteUser.equals(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only delete your own accounts.");
        }
        Account primaryAccount = accountDao.getPrimaryAccount(user);
        if (accountToDelete.equals(primaryAccount)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your active account.");
        }
        try {
            accountDao.deleteAccount(accountDao.getAccountById(id), user);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No account with ID " + id + ".");
        }
    }

    /**
     * Sets the primary account of the user
     * @param id The account ID of one of the user's accounts
     * @param principal The logged in user
     * @return The account that has been set to primary
     */
    @RequestMapping(path = "/accounts/{id}", method = RequestMethod.PUT)
    public Account setPrimaryAccount(@PathVariable int id, Principal principal) {
        Account updatedAccount;
        if (accountDao.getAccountById(id).getUserId() == userDao.getUserByUsername(principal.getName()).getId()) {
            try {
                updatedAccount = accountDao.setPrimaryAccount(accountDao.getAccountById(id));
            } catch (DaoException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "That account was not found.");
            }
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only change the primary accounts for yourself.");
        return updatedAccount;
    }

    /**
     * Get the user of a specific account
     * @param id The account ID
     * @return The User in the form of a Dto which includes the User ID and Username
     */
    @RequestMapping(path = "accounts/{id}/user", method = RequestMethod.GET)
    public UserDto getUserOfAccount(@PathVariable int id) {
        UserDto userDto = new UserDto();
        User user = null;
        try {
            user = accountDao.getUserOfAccount(accountDao.getAccountById(id));
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "That account was not found.");
        }
        if (user != null) {
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
        }
        return userDto;
    }

    //TODO: Move into UserController

    /**
     * Get a list of all users
     * @param principal The logged in user
     * @return All users except the logged in user
     */
    @RequestMapping(path = "users", method = RequestMethod.GET)
    public List<UserDto> getAllUsers(Principal principal) {
        List<User> allUsers = null;
        try {
            allUsers = userDao.getUsers(principal.getName());
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<UserDto> allUserDto = new ArrayList<>();
        for (User user : allUsers) {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            allUserDto.add(userDto);
        }
        return allUserDto;
    }

    @RequestMapping(path = "/accounts/{id}", method = RequestMethod.GET)
    public Account getAccountById(@PathVariable int id, Principal principal) {
        Account account;
        if (accountDao.getAccountById(id).getUserId() == userDao.getUserByUsername(principal.getName()).getId()) {
            try {
                account = accountDao.getAccountById(id);
            } catch (DaoException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "That account was not found.");
            }
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only view your own accounts.");
        return account;
    }

    //TODO: Make getters of Accounts and accountsByID


}