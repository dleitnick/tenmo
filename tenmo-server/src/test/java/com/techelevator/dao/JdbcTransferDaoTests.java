package com.techelevator.dao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.exception.InsufficientFundsException;
import com.techelevator.tenmo.exception.InvalidAccountException;
import com.techelevator.tenmo.model.transfer.Transfer;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static com.techelevator.dao.JdbcAccountDaoTests.*;
import static com.techelevator.dao.JdbcUserDaoTests.USER_1;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("JdbcTransferDao Tests")
class JdbcTransferDaoTests extends BaseDaoTests {

    private JdbcTransferDao sut;
    private JdbcAccountDao accountSut;
    protected static final Transfer TRANSFER_1 = new Transfer (2, 2, 2001, 2002, new BigDecimal("100.50"));   //transfer_id 3001
    protected static final Transfer TRANSFER_2 = new Transfer (2, 2, 2003, 2001, new BigDecimal("1000.00"));  //transfer_id 3002
    protected static final Transfer TRANSFER_3 = new Transfer (2, 2, 2002, 2001, new BigDecimal("99.99"));    //transfer_id 3003
    protected static final Transfer TRANSFER_4 = new Transfer (2, 2, 2001, 2002, new BigDecimal("50.00"));    //transfer_id 3004
    protected static final Transfer TRANSFER_5 = new Transfer (1, 3, 2001, 2002, new BigDecimal("50.00"));    //transfer_id 3005
    protected static final Transfer TRANSFER_6 = new Transfer (1, 1, 2001, 2002, new BigDecimal("50.00"));    //transfer_id 3006
    protected static final Transfer TRANSFER_7 = new Transfer (2, 3, 2001, 2002, new BigDecimal("50.00"));    //transfer_id 3007
    protected static final Transfer TRANSFER_8 = new Transfer (1, 2, 2001, 2002, new BigDecimal("50.00"));    //transfer_id 3008
    protected static final Transfer NEW_TRANSFER = new Transfer (2,2, 2001, 2003, new BigDecimal("750.00"));    //transfer_id 3009
    protected static final Transfer INVALID_ACCOUNT_TRANSFER = new Transfer(2,2, 2122, 2003, new BigDecimal("100.00"));
    protected static final Transfer VALID_REQUEST_TRANSFER = new Transfer(1, 1, 2001, 2003, new BigDecimal("500.00") );
    protected static final Transfer INVALID_REQUEST_ZERO_TRANSFER = new Transfer(1, 1, 2001, 2003, new BigDecimal("0.00"));
    protected static final Transfer INVALID_REQUEST_NEGATIVE_TRANSFER = new Transfer(1, 1, 2001, 2003, new BigDecimal("-100.00"));

    @BeforeEach
    void setup() {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcTransferDao(jdbcTemplate, namedParameterJdbcTemplate);
        accountSut = new JdbcAccountDao(jdbcTemplate);
    }

    @Test
    @DisplayName("01. Transfer verification returns true for valid accounts")
    void transfer_verification_returns_true_for_valid_accounts() {
        assertTrue(sut.transactionVerification(TRANSFER_1));
        assertTrue(sut.transactionVerification(TRANSFER_2));
        assertTrue(sut.transactionVerification(TRANSFER_3));
        assertTrue(sut.transactionVerification(TRANSFER_4));
    }

    @Test
    @DisplayName("02. Transfer verification returns false for invalid accounts")
    void transfer_verification_returns_false_for_invalid_accounts() {
        assertFalse(sut.transactionVerification(INVALID_ACCOUNT_TRANSFER));
    }

    @Test
    @DisplayName("03. Getting a transfer with valid ID returns correct transfer")
    void getTransferById_returns_transfer_for_valid_id() {
        TRANSFER_2.setTransferId(3002);
        assertEquals(TRANSFER_2, sut.getTransferById(3002), "The returned transfer is incorrect");
    }

    @Test
    @DisplayName("04. Getting a transfer with invalid ID throws DaoException")
    void getTransferById_throws_DaoException_for_invalid_id() {
        assertThrows(DaoException.class, () -> {
            sut.getTransferById(-3);
        });
    }

    @Test
    @DisplayName("05. Finalizing transfer returns transfer and correct balances")
    void finalizeTransfer_returns_transfer_and_correct_account_balances() {
        BigDecimal transferAmount = TRANSFER_1.getAmount();
        BigDecimal account2001Balance = ACCOUNT_1.getBalance();
        account2001Balance = account2001Balance.subtract(transferAmount);
        BigDecimal account2002Balance = ACCOUNT_2.getBalance();
        account2002Balance = account2002Balance.add(transferAmount);
        TRANSFER_1.setTransferId(3001);

        Transfer completedTransfer = sut.finalizeTransfer(TRANSFER_1);
        assertEquals(account2001Balance, accountSut.getAccountById(TRANSFER_1.getAccountFrom()).getBalance());
        assertEquals(account2002Balance, accountSut.getAccountById(TRANSFER_1.getAccountTo()).getBalance());
        assertEquals(TRANSFER_1, completedTransfer, "Transfers are different!");
    }

    @Test
    @DisplayName("06. Finalize transfer where transfer doesn't exist throws DaoException")
    void finalizeTransfer_where_transfer_doesnt_exist_throws_DaoException() {
        assertThrows(DaoException.class, () -> {
            sut.finalizeTransfer(NEW_TRANSFER);
        });
    }

    @Test
    @DisplayName("07. Finalize transfer with invalid account throws InvalidAccountException")
    void finalizeTransfer_with_invalid_account_throws_InvalidAccountException() {
        TRANSFER_1.setAccountTo(3899);
        assertThrows(InvalidAccountException.class, () -> {
            sut.finalizeTransfer(TRANSFER_1);
        });
    }

    @Test
    @DisplayName("08. Finalize transfer with invalid transfer amount throws IllegalArgumentException")
    void finalizeTransfer_with_invalid_transfer_amount_throws_IllegalArgumentException() {
        TRANSFER_3.setTransferId(3003);
        TRANSFER_3.setAmount(new BigDecimal("-100.00"));
        assertThrows(IllegalArgumentException.class, () -> {
            sut.finalizeTransfer(TRANSFER_3);
        });
    }

    @Test
    @DisplayName("09. Finalize transfer with insufficient funds throws InsufficientFundsException")
    void finalizeTransfer_with_insufficient_throws_InsufficientFundsException() {
        assertThrows(InsufficientFundsException.class, () -> {
            sut.finalizeTransfer(TRANSFER_2);
        });
    }

    @Test
    @DisplayName("10. Sending a valid transfer returns transfer and updates account balances")
    void sending_valid_transfer_returns_transfer_and_updates_account_balances() {
        BigDecimal transferAmount = NEW_TRANSFER.getAmount();
        BigDecimal account2001Balance = ACCOUNT_1.getBalance();
        account2001Balance = account2001Balance.subtract(transferAmount);
        BigDecimal account2003Balance = ACCOUNT_3.getBalance();
        account2003Balance = account2003Balance.add(transferAmount);

        Transfer completedTransfer = sut.handleTransfer(NEW_TRANSFER);
        NEW_TRANSFER.setTransferId(3009); // First number after test-data.sql inserts
        assertEquals(account2001Balance, accountSut.getAccountById(NEW_TRANSFER.getAccountFrom()).getBalance());
        assertEquals(account2003Balance, accountSut.getAccountById(NEW_TRANSFER.getAccountTo()).getBalance());
        assertEquals(NEW_TRANSFER, completedTransfer, "Transfers are different!");
    }

    @Test
    @DisplayName("11. Getting all transfers for user returns correct number of transfers")
    void getting_all_transfers_for_user_returns_correct_number_of_transfers() {
        int transferListSize = sut.getTransfersByUser(USER_1, 0, 0).size();
        assertEquals(8, transferListSize);
    }

    @Test
    @DisplayName("12. Getting all request transfers for user returns correct number of transfers")
    void getting_all_request_transfers_for_user_returns_correct_number_of_transfers() {
        int transferListSize = sut.getTransfersByUser(USER_1, 1, 0).size();
        assertEquals(3, transferListSize);
    }

    @Test
    @DisplayName("13. Getting all rejected transfers for user returns correct number of transfers")
    void getting_all_rejected_transfers_for_user_returns_correct_number_of_transfers() {
        int transferListSize = sut.getTransfersByUser(USER_1, 0, 3).size();
        assertEquals(2, transferListSize);
    }

    @Test
    @DisplayName("13. Getting all requested transfers in pending status for user returns correct number of transfers")
    void getting_all_pending_requested_transfers_for_user_returns_correct_number_of_transfers() {
        int transferListSize = sut.getTransfersByUser(USER_1, 1, 1).size();
        assertEquals(1, transferListSize);
    }

    @Test
    @DisplayName("14. Getting all approved transfers for user returns correct number of transfers")
    void getting_all_approved_transfers_for_user_returns_correct_number_of_transfers() {
        int transferListSize = sut.getTransfersByUser(USER_1, 0, 2).size();
        assertEquals(5, transferListSize);
    }

    @Test
    @DisplayName("15. Requesting a valid transfer returns transfer, doesn't update balances and returns status id of pending")
    void requesting_valid_transfer_returns_correct_results() {
        BigDecimal transferAmount = VALID_REQUEST_TRANSFER.getAmount();
        BigDecimal account2003Balance = ACCOUNT_3.getBalance();
        BigDecimal account2001Balance = ACCOUNT_1.getBalance();

        Transfer requestedTransfer = sut.handleTransfer(VALID_REQUEST_TRANSFER);
        VALID_REQUEST_TRANSFER.setTransferId(requestedTransfer.getTransferId()); // First number after test-data.sql inserts
        assertEquals(account2001Balance, accountSut.getAccountById(VALID_REQUEST_TRANSFER.getAccountFrom()).getBalance());
        assertEquals(account2003Balance, accountSut.getAccountById(VALID_REQUEST_TRANSFER.getAccountTo()).getBalance());
        assertEquals(VALID_REQUEST_TRANSFER, requestedTransfer, "Transfers are different!");
        assertEquals("pending", sut.getTransferStatusById(requestedTransfer.getTransferStatusId()).getTransferStatusDescription().toLowerCase());
    }

    @Test
    @DisplayName("16. Requesting 0 or negative throws exception")
    void requesting_0_or_negative_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> {
            sut.handleTransfer(INVALID_REQUEST_ZERO_TRANSFER);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            sut.handleTransfer(INVALID_REQUEST_NEGATIVE_TRANSFER);
        });
    }

    @Test
    @DisplayName("17. Accepting a requested transfer returns transfer, updates balances, and returns status of approved")
    void accepting_transfer_returns_correct_results() {
        //Request is from account 2001 to 2002 for 50.00
        BigDecimal transferAmount = TRANSFER_6.getAmount();
        BigDecimal account2001Balance = ACCOUNT_1.getBalance();
        account2001Balance = account2001Balance.subtract(transferAmount);
        BigDecimal account2002Balance = ACCOUNT_2.getBalance();
        account2002Balance = account2002Balance.add(transferAmount);

        TRANSFER_6.setTransferId(3006);
        TRANSFER_6.setTransferStatusId(sut.getTransferStatusByDesc("approved").getTransferStatusId());

        Transfer requestedTransfer = sut.handleTransfer(TRANSFER_6);
        VALID_REQUEST_TRANSFER.setTransferId(requestedTransfer.getTransferId()); // First number after test-data.sql inserts
        assertEquals(account2001Balance, accountSut.getAccountById(TRANSFER_6.getAccountFrom()).getBalance());
        assertEquals(account2002Balance, accountSut.getAccountById(TRANSFER_6.getAccountTo()).getBalance());
        assertEquals(TRANSFER_6, requestedTransfer, "Transfers are different!");
        assertEquals("approved", sut.getTransferStatusById(requestedTransfer.getTransferStatusId()).getTransferStatusDescription().toLowerCase());
    }

    @Test
    @DisplayName("18. Rejecting a requested transfer returns transfer, doesn't update balances, and returns status of rejected")
    void rejecting_transfer_returns_correct_results() {
        //Request is from account 2001 to 2002 for 50.00
        BigDecimal transferAmount = TRANSFER_6.getAmount();
        BigDecimal account2001Balance = ACCOUNT_1.getBalance();
        BigDecimal account2002Balance = ACCOUNT_2.getBalance();

        TRANSFER_6.setTransferId(3006);
        TRANSFER_6.setTransferStatusId(sut.getTransferStatusByDesc("rejected").getTransferStatusId());

        Transfer requestedTransfer = sut.handleTransfer(TRANSFER_6);
        VALID_REQUEST_TRANSFER.setTransferId(requestedTransfer.getTransferId()); // First number after test-data.sql inserts
        assertEquals(account2001Balance, accountSut.getAccountById(TRANSFER_6.getAccountFrom()).getBalance());
        assertEquals(account2002Balance, accountSut.getAccountById(TRANSFER_6.getAccountTo()).getBalance());
        assertEquals(TRANSFER_6, requestedTransfer, "Transfers are different!");
        assertEquals("rejected", sut.getTransferStatusById(requestedTransfer.getTransferStatusId()).getTransferStatusDescription().toLowerCase());
    }
}
