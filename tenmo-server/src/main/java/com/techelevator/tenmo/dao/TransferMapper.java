package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.transfer.Transfer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferMapper implements RowMapper<Transfer> {
    @Override
    public Transfer mapRow(ResultSet results, int i) throws SQLException {
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
