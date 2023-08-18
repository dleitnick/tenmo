package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class AccountService {
    private final String BASE_URL;
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public AccountService(String url, String authToken) {
        this.BASE_URL = url;
        this.authToken = authToken;
    }

    public Account getPrimaryAccountBalance() {
        Account account = null;
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(BASE_URL + "balance", HttpMethod.GET, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }
    
    public Account getAccountById(int id) {
        Account account = null;
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(BASE_URL + "accounts/" + id, HttpMethod.GET, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    public Account[] getAllAccountsBalance() {
        Account[] accounts = null;
        try {
            ResponseEntity<Account[]> response =
                    restTemplate.exchange(BASE_URL + "balance/all", HttpMethod.GET, makeAuthEntity(), Account[].class);
            accounts = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return accounts;
    }

    public Account createNewAccount() {
        Account account = null;
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(BASE_URL + "accounts", HttpMethod.POST, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    public boolean deleteAccount(int id) {
        boolean success = false;
        try {
            restTemplate.exchange(BASE_URL + "accounts/" + id, HttpMethod.DELETE, makeAuthEntity(), Void.class);
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    public Account setPrimaryAccount(int id) {
        Account account = null;
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(BASE_URL + "accounts/" + id, HttpMethod.PUT, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    public User getUserOfAccount(int id) {
        User user = null;
        try {
            ResponseEntity<User> response =
                    restTemplate.exchange(BASE_URL + "accounts/" + id + "/user", HttpMethod.GET, makeAuthEntity(), User.class);
            user = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return user;
    }

    private HttpEntity<Account> makeAccountEntity(Account account) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(account, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

}
