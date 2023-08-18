package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.exception.InvalidAccountException;
import com.techelevator.tenmo.exception.InsufficientFundsException;
import com.techelevator.tenmo.model.transfer.Transfer;
import com.techelevator.tenmo.model.transfer.TransferDto;
import com.techelevator.tenmo.model.transfer.TransferStatus;
import com.techelevator.tenmo.model.transfer.TransferType;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final AccountDao accountDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.accountDao = new JdbcAccountDao(jdbcTemplate);
    }

    @Override
    public Transfer getTransferById(int id) {
        Transfer transfer = null;
        String sql = "SELECT * FROM transfer WHERE transfer_id=?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            } else throw new DaoException("No transfer with ID: " + id);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return transfer;
    }

    @Override
    public List<Transfer> getTransfersByUser(User user, int transferTypeId, int transferStatusId) {

        String sql = "SELECT * FROM transfer\n" +
                "WHERE :accountId IN (account_from, account_to)\n" +
                "AND transfer_type_id IN (:transferTypes)\n" +
                "AND transfer_status_id IN (:transferStatus);";
        List<TransferType> transferTypeList = getTransferTypeList();
        List<Integer> transferTypeIdList = new ArrayList<>();
        for (TransferType transferType : transferTypeList) {
            transferTypeIdList.add(transferType.getTransferTypeId());
        }
        List<TransferStatus> transferStatusList = getTransferStatusList();
        List<Integer> transferStatusIdList = new ArrayList<>();
        for (TransferStatus transferStatus : transferStatusList) {
            transferStatusIdList.add(transferStatus.getTransferStatusId());
        }
        MapSqlParameterSource inQueryParams = new MapSqlParameterSource();
        // Update this to allow for all accounts from user????
        inQueryParams.addValue("accountId", accountDao.getPrimaryAccount(user).getAccountId());
        if (transferTypeId == 0) {
            inQueryParams.addValue("transferTypes", transferTypeIdList);
        } else {
            inQueryParams.addValue("transferTypes", transferTypeId);
        }
        if (transferStatusId == 0) {
            inQueryParams.addValue("transferStatus", transferStatusIdList);
        } else {
            inQueryParams.addValue("transferStatus", transferStatusId);
        }
        List<Transfer> transfers = null;
        try {
            transfers = namedParameterJdbcTemplate.query(sql, inQueryParams, new TransferMapper());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return transfers;
    }

    //TODO: possibly change check constraints in database since we handled them here
    @Override
    public Transfer handleTransfer(Transfer transfer) {
        Transfer potentialTransfer = null;
        Integer newTransferId = transfer.getTransferId();
        if(transfer.getAmount().compareTo(new BigDecimal("0.00")) < 1) throw new IllegalArgumentException("Cannot transfer negative or 0 amount.");
        if (newTransferId == 0) {
            String sql = "INSERT INTO transfer " +
                    "(transfer_type_id, transfer_status_id, account_from, account_to, amount, transfer_created) " +
                    "VALUES (?, ?, ?, ?, ?, NOW()) RETURNING transfer_id;";
            try {
                newTransferId = jdbcTemplate.queryForObject(
                        sql, Integer.class, transfer.getTypeId(), transfer.getTransferStatusId(),
                        transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
            } catch (CannotGetJdbcConnectionException e) {
                throw new DaoException("Unable to connect to server or database", e);
            } catch (DataIntegrityViolationException e) {
                throw new DaoException("Data integrity violation", e);
            }
        }
        if (newTransferId != null && transfer.getTransferStatusId() == getTransferStatusByDesc("pending").getTransferStatusId()) {
            potentialTransfer = getTransferById(newTransferId);
        } else if (transfer.getTransferId() != 0) {
            potentialTransfer = finalizeTransfer(transfer);
        } else if (newTransferId != null) {
            potentialTransfer = finalizeTransfer(getTransferById(newTransferId));
        }
        return potentialTransfer;
    }

    @Override
    public Transfer finalizeTransfer(Transfer transfer) {
        Transfer updatedTransfer = null;
        String sql = "UPDATE transfer SET transfer_status_id = ?, transfer_completed = NOW() WHERE transfer_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, transfer.getTransferStatusId(), transfer.getTransferId());
            if (rowsAffected == 0) {
                throw new DaoException("Zero rows affected, expected at least one");
            } else if (transfer.getTransferStatusId() == getTransferStatusByDesc("approved").getTransferStatusId()){
                if (transactionVerification(transfer)) {
                    accountDao.subtractFromBalance(accountDao.getAccountById(transfer.getAccountFrom()), transfer.getAmount());
                    accountDao.addToBalance(accountDao.getAccountById(transfer.getAccountTo()), transfer.getAmount());
                } else {
                    throw new InvalidAccountException("One of the accounts is not a valid account.");
                }
            }
            updatedTransfer = getTransferById(transfer.getTransferId());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation: ", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (InsufficientFundsException e) {
            throw new InsufficientFundsException(e.getMessage());
        } catch (InvalidAccountException e) {
            throw new InvalidAccountException(e.getMessage());
        }
        return updatedTransfer;
    }

    @Override
    public boolean transactionVerification(Transfer transfer) {
        try {
            accountDao.getAccountById(transfer.getAccountTo());
            accountDao.getAccountById(transfer.getAccountFrom());
        } catch (DaoException e) {
            return false;
        }
        return true;
    }

    @Override
    public List<TransferStatus> getTransferStatusList() {
        List<TransferStatus> listOfTransferStatus = new ArrayList<>();
        String sql = "SELECT * FROM transfer_status;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                TransferStatus transferStatus = new TransferStatus();
                transferStatus.setTransferStatusId(results.getInt("transfer_status_id"));
                transferStatus.setTransferStatusDescription(results.getString("transfer_status_desc"));
                listOfTransferStatus.add(transferStatus);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return listOfTransferStatus;
    }

    @Override
    public TransferStatus getTransferStatusById(int id) {
        TransferStatus transferStatus = new TransferStatus();
        String sql = "SELECT * FROM transfer_status WHERE transfer_status_id=?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if (results.next()) {
            transferStatus.setTransferStatusId(results.getInt("transfer_status_id"));
            transferStatus.setTransferStatusDescription(results.getString("transfer_status_desc"));
        } else transferStatus = null;
        return transferStatus;
    }

    @Override
    public TransferStatus getTransferStatusByDesc(String desc) {
        TransferStatus transferStatus = new TransferStatus();
        String sql = "SELECT * FROM transfer_status WHERE transfer_status_desc ILIKE ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, desc);
        if (results.next()) {
            transferStatus.setTransferStatusId(results.getInt("transfer_status_id"));
            transferStatus.setTransferStatusDescription(results.getString("transfer_status_desc"));
        } else transferStatus = null;
        return transferStatus;
    }


    @Override
    public List<TransferType> getTransferTypeList() {
        List<TransferType> listOfTransferType = new ArrayList<>();
        String sql = "SELECT * FROM transfer_type;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                TransferType transferType = new TransferType();
                transferType.setTransferTypeId(results.getInt("transfer_type_id"));
                transferType.setTransferTypeDescription(results.getString("transfer_type_desc"));
                listOfTransferType.add(transferType);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return listOfTransferType;
    }

    @Override
    public TransferType getTransferTypeById(int id) {
        TransferType transferType = new TransferType();
        String sql = "SELECT * FROM transfer_type WHERE transfer_type_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if (results.next()) {
            transferType.setTransferTypeId(results.getInt("transfer_type_id"));
            transferType.setTransferTypeDescription(results.getString("transfer_type_desc"));
        } else transferType = null;
        return transferType;
    }

    @Override
    public TransferType getTransferTypeByDesc(String desc) {
        TransferType transferType = new TransferType();
        String sql = "SELECT * FROM transfer_type WHERE transfer_type_desc ILIKE ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, desc);
        if (results.next()) {
            transferType.setTransferTypeId(results.getInt("transfer_type_id"));
            transferType.setTransferTypeDescription(results.getString("transfer_type_desc"));
        } else transferType = null;
        return transferType;
    }


    private Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTypeId(results.getInt("transfer_type_id"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setAccountFrom(results.getInt("account_from"));
        transfer.setAccountTo(results.getInt("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        transfer.setTransferCreated(results.getTimestamp("transfer_created"));
        if (results.getTimestamp("transfer_completed") != null) {
            transfer.setTransferCompleted(results.getTimestamp("transfer_completed"));
        }
        return transfer;
    }
}
