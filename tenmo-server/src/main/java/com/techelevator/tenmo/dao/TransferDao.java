package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.transfer.Transfer;
import com.techelevator.tenmo.model.transfer.TransferStatus;
import com.techelevator.tenmo.model.transfer.TransferType;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface TransferDao {

    /**
     * Get a transfer by an ID
     * @param id Transfer ID
     * @return The Transfer
     */
    Transfer getTransferById(int id);

    /**
     * Get a list of Transfers of a User
     * @param user User
     * @param transferTypeId Type of Transfer
     * @param transferStatusId Status of Transfer
     * @return A list of Transfers
     */
    List<Transfer> getTransfersByUser(User user, int transferTypeId, int transferStatusId);

    /**
     * Handle the logic of a transfer. Decides if the transfer should be finalized.
     * @param transfer Transfer
     * @return The updated Transfer which should include a Transfer ID and creation timestamp
     */
    Transfer handleTransfer(Transfer transfer);

    /**
     * Finalize a transfer based on it's status
     * @param transfer Transfer
     * @return The updated Transfer which should include a completed timestamp
     */
    Transfer finalizeTransfer(Transfer transfer);

    /**
     * Verify the accounts listed in the transfer are valid
     * @param transfer Transfer
     * @return A boolean verifying the transfer validity
     */
    boolean transactionVerification(Transfer transfer);

    /**
     * Get a list of all the different Transfer status
     * @return A TransferStatus list
     */
    List<TransferStatus> getTransferStatusList();

    /**
     * Get the TransferStatus by ID
     * @param id TransferStatus ID
     * @return The TransferStatus
     */
    TransferStatus getTransferStatusById(int id);

    /**
     * Get TransferStatus by description
     * @param desc Description of TransferStatus
     * @return The TransferStatus
     */
    TransferStatus getTransferStatusByDesc(String desc);

    /**
     * Get a list of all the different Transfer types
     * @return A TransferType list
     */
    List<TransferType> getTransferTypeList();

    /**
     * Get the TransferType by ID
     * @param id TransferType ID
     * @return The TransferType
     */
    TransferType getTransferTypeById(int id);

    /**
     * Get the TransferType by description
     * @param desc Description of the TransferType
     * @return The TransferType
     */
    TransferType getTransferTypeByDesc(String desc);
}
