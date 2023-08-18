package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.exception.InsufficientFundsException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.techelevator.dao.JdbcUserDaoTests.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("JdbcAccountDao Tests")
class JdbcAccountDaoTests extends BaseDaoTests {

    private JdbcAccountDao sut;
    private JdbcUserDao userDaoSut;



    protected static final Account ACCOUNT_1 = new Account(2001, USER_1.getId(), new BigDecimal("1000.00"), true);
    protected static final Account USER_1_SECOND_ACCOUNT = new Account(2004, USER_1.getId(), new BigDecimal("200.00"), false);
    protected static final Account ACCOUNT_2 = new Account(2002, USER_2.getId(), new BigDecimal("500.00"), true);
    protected static final Account ACCOUNT_3 = new Account(2003, USER_3.getId(), new BigDecimal("0.00"), true);
    protected static final Account SECONDARY_ACCOUNT = new Account(2005, USER_1.getId(), new BigDecimal("0.00"), false);


    @BeforeEach
    void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcAccountDao(jdbcTemplate);
        userDaoSut = new JdbcUserDao(jdbcTemplate);
    }

    @Test
    @DisplayName("01. Get account with valid ID returns the account")
    void get_account_with_valid_id_returns_the_account() {
        assertEquals(ACCOUNT_1, sut.getAccountById(ACCOUNT_1.getAccountId()), "Account returned is not the correct account.");
    }

    @Test
    @DisplayName("02. Get account by valid ID throws DaoException")
    void get_account_by_valid_id_throws_DaoException() {
        assertThrows(DaoException.class, () -> {
            sut.getAccountById(22398);
        });
    }

    @Test
    @DisplayName("03. Get accounts with valid user returns accounts")
    void get_accounts_with_valid_user_returns_accounts() {
        List<Account> accountList = new ArrayList<>();
        accountList.add(ACCOUNT_1);
        accountList.add(USER_1_SECOND_ACCOUNT);
        assertEquals(accountList, sut.getAccounts(USER_1), "Account returned is not the correct account.");
    }

    @Test
    @DisplayName("04. Get accounts with invalid user throws DaoException")
    void get_accounts_with_invalid_user_throws_DaoException() {
        assertThrows(DaoException.class, () -> {
            sut.getAccounts(new User());
        });
    }

    @Test
    @DisplayName("05. Adding a positive value to balance returns new balance")
    void adding_positive_value_to_balance_returns_new_balance() {
        sut.addToBalance(ACCOUNT_1, new BigDecimal("500.00"));
        assertEquals(new BigDecimal("1500.00"), ACCOUNT_1.getBalance());
    }

    @Test
    @DisplayName("06. Adding a negative value to balance throws illegal argument exception")
    void adding_negative_value_to_balance_throws_illegal_argument_exception() {
        assertThrows(IllegalArgumentException.class, () -> {
            sut.addToBalance(ACCOUNT_1, new BigDecimal("-10.00"));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            sut.addToBalance(ACCOUNT_1, new BigDecimal("0.00"));
        });
    }

    @Test
    @DisplayName("07. Adding no value to balance throws illegal argument exception")
    void adding_no_value_to_balance_throws_illegal_argument_exception() {
        assertThrows(IllegalArgumentException.class, () -> {
            sut.addToBalance(ACCOUNT_1, new BigDecimal("0.00"));
        });
    }

    @Test
    @DisplayName("08. Subtracting a positive value from balance returns new balance")
    void subtracting_positive_value_from_balance_returns_new_balance() {
        sut.subtractFromBalance(ACCOUNT_2, new BigDecimal("400.00"));
        assertEquals(new BigDecimal("100.00"), ACCOUNT_2.getBalance());
    }

    @Test
    @DisplayName("09. Subtracting full balance returns zero balance")
    void subtracting_full_balance_returns_zero_balance() {
        sut.subtractFromBalance(ACCOUNT_2, new BigDecimal("500.00"));
        assertEquals(new BigDecimal("00.00"), ACCOUNT_2.getBalance());
    }

    @Test
    @DisplayName("10. Subtracting negative value from balance throws IllegalArgumentException")
    void subtracting_negative_value_from_balance_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            sut.subtractFromBalance(ACCOUNT_2, new BigDecimal("-500.00"));
        });
    }

    @Test
    @DisplayName("11. Subtracting value from balance returning negative balance throws InsufficientFundsException")
    void subtracting_value_from_balance_returning_negative_balance_throws_InsufficientFundsException() {
        assertThrows(InsufficientFundsException.class, () -> {
            sut.subtractFromBalance(ACCOUNT_3, new BigDecimal("1.00"));
        });
    }

    @Test
    @DisplayName("12. Get user of account returns valid user")
    void getting_account_user_returns_valid_user() {
        assertEquals(USER_1.getUsername(), sut.getUserOfAccount(ACCOUNT_1).getUsername());
    }

    @Test
    @DisplayName("13. Get user of invalid account returns null user")
    void getting_invalid_account_user_returns_null_user() {
        assertNull(sut.getUserOfAccount(new Account(9999, 23, new BigDecimal("1000.00"), true)));
    }

    @Test
    @DisplayName("14. Creating new account returns new account not set to primary and with 0 balance")
    void creating_new_account_returns_correct_account() {
        Account createdAccount = sut.createNewAccount(USER_1);
        assertEquals(SECONDARY_ACCOUNT.getUserId(), createdAccount.getUserId());
        assertEquals(SECONDARY_ACCOUNT.getBalance(), createdAccount.getBalance());
        assertEquals(SECONDARY_ACCOUNT.isPrimary(), createdAccount.isPrimary());
    }

    @Test
    @DisplayName("15. Creating new account with invalid User throws DaoException")
    void creating_new_account_with_invalid_user_throws_DaoException() {
        assertThrows(DaoException.class, () -> {
            sut.createNewAccount(new User(88, "fake", "password", "none"));
        });
    }

    @Test
    @DisplayName("16. Changing primary account sets all other accounts from user to false")
    void changing_primary_accounts_returns_correct_results() {
        sut.setPrimaryAccount(USER_1_SECOND_ACCOUNT);
        assertTrue(sut.getAccountById(USER_1_SECOND_ACCOUNT.getAccountId()).isPrimary());
        assertFalse(sut.getAccountById(ACCOUNT_1.getAccountId()).isPrimary());
    }

    @Test
    @Disabled
    @DisplayName("17. Deleting account removes account from accounts and transfer money to primary account")
    void deleting_account_behaves_properly() {
//        sut.setPrimaryAccount(ACCOUNT_1);
        BigDecimal expectedBalance = ACCOUNT_1.getBalance().add(USER_1_SECOND_ACCOUNT.getBalance());
        sut.deleteAccountForTesting(USER_1_SECOND_ACCOUNT, USER_1);
        assertEquals(expectedBalance, sut.getAccountById(ACCOUNT_1.getAccountId()).getBalance());
        List<Account> accountList = new ArrayList<>();
        ACCOUNT_1.setBalance(expectedBalance);
        accountList.add(ACCOUNT_1);
        assertEquals(accountList, sut.getAccounts(USER_1), "Account returned is not the correct account.");
    }
}
