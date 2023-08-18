package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.exception.InsufficientFundsException;
import com.techelevator.tenmo.exception.InvalidAccountException;
import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.model.transfer.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class TransferController {

    private TransferDao transferDao;
    private AccountDao accountDao;
    private UserDao userDao;

    public TransferController(TransferDao transferDao, AccountDao accountDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    /**
     * Get a specific transfer of the user
     * @param id The transfer ID
     * @param principal The logged in user
     * @return A TransferDto which includes everything from a Transfer and adds the usernames of the
     * accounts and timestamps for creation/completion
     */
    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.GET)
    public TransferDto getTransfer(@PathVariable int id, Principal principal) {
        Transfer transfer = transferDao.getTransferById(id);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No transfer found");
        } else if (!verifyTransferBelongToUser(transfer, principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Transfer doesn't belong to you.");
        } else return transferToDto(transfer);
    }

    /**
     * Get all the user's transfers
     * @param typeId (Optional) The specific type of the transfers
     * @param statusId (Optional) The specific status of the transfers
     * @param principal The logged in user
     * @return A list of TransferDtos which includes everything from a Transfer and adds the usernames of the
     * accounts and timestamps for creation/completion
     */
    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<TransferDto> getTransfers(@RequestParam(required = false, defaultValue = "0") int typeId, @RequestParam(required = false, defaultValue = "0") int statusId, Principal principal) {
        List<Transfer> transfersList = new ArrayList<>();
        List<TransferDto> transferDtoList = new ArrayList<>();
        transfersList = transferDao.getTransfersByUser(userDao.getUserByUsername(principal.getName()), typeId, statusId);
        if (transfersList.size() < 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No transfers found");
        }
        for (Transfer transfer : transfersList) {
            transferDtoList.add(transferToDto(transfer));
        }
        return transferDtoList;
    }

    /**
     * Send a transfer to another user. This transfer has to be to a valid user and cannot reduce the
     * user's account balance below zero. It will be immediately approved and finalized.
     * @param transferSendDto A dto that contains the user being sent the transfer and the amount to send
     * @param principal The logged in user
     * @return A TransferSendDto that includes usernames of account holders, the new balance of the user,
     * and the timestamp of the completion
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/send", method = RequestMethod.POST)
    public TransferSendDto sendTransfer(@RequestBody TransferSendDto transferSendDto, Principal principal) {
        BigDecimal moneyToSubtract = transferSendDto.getAmountSent();
        BigDecimal currentBalance = accountDao.getPrimaryAccount(userDao.getUserByUsername(principal.getName())).getBalance();
        BigDecimal potentialBalance = currentBalance.subtract(moneyToSubtract);
        if (moneyToSubtract.compareTo(new BigDecimal("0.00")) > 0 && potentialBalance.compareTo(new BigDecimal("0.00")) > -1) {
            User user = userDao.getUserByUsername(principal.getName());
            User receivingUser = userDao.getUserByUsername(transferSendDto.getReceiverName());
            if (receivingUser == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, transferSendDto.getReceiverName() + " is not a valid user.");
            Transfer transfer = new Transfer();
            transfer.setTypeId(transferDao.getTransferTypeByDesc("send").getTransferTypeId());
            transfer.setTransferStatusId(transferDao.getTransferStatusByDesc("approved").getTransferStatusId());
            transfer.setAccountFrom(accountDao.getPrimaryAccount(user).getAccountId());
            if (transferSendDto.getAccountId() > 0) {
                transfer.setAccountTo(transferSendDto.getAccountId());
            } else {
                transfer.setAccountTo(accountDao.getPrimaryAccount(receivingUser).getAccountId());
            }
            transfer.setAmount(transferSendDto.getAmountSent());
            if (transfer.getAccountTo() == transfer.getAccountFrom()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot send money to yourself!");
            Transfer completedTransfer = null;
            try {
                completedTransfer = transferDao.handleTransfer(transfer);
            } catch (DaoException | IllegalArgumentException | InsufficientFundsException | InvalidAccountException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            return transferToSendDto(completedTransfer, user, receivingUser);
        } else if (potentialBalance.compareTo(new BigDecimal("0.00")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The money to subtract should be positive.");
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The transaction would cause a negative balance.");
    }

    /**
     * Create a request for another user to transfer the user a specified amount
     * @param transferRequestDto A dto that contains the user being requested from and the amount requested
     * @param principal The logged in user
     * @return A TransferRequestDto that includes usernames of account holders and a creation timestamp
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/requests", method = RequestMethod.POST)
    public TransferRequestDto requestTransfer(@RequestBody TransferRequestDto transferRequestDto, Principal principal) {
        User user = userDao.getUserByUsername(principal.getName());
        User sendingUser = userDao.getUserByUsername(transferRequestDto.getPayerName());
        if (sendingUser == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, transferRequestDto.getRequesterName() + " is not a valid user.");
        Transfer transfer = new Transfer();
        transfer.setTypeId(transferDao.getTransferTypeByDesc("request").getTransferTypeId());
        transfer.setTransferStatusId(transferDao.getTransferStatusByDesc("pending").getTransferStatusId());
        transfer.setAccountFrom(accountDao.getPrimaryAccount(sendingUser).getAccountId());
        transfer.setAccountTo(accountDao.getPrimaryAccount(user).getAccountId());
        transfer.setAmount(transferRequestDto.getAmountRequested());
        if (transfer.getAccountTo() == transfer.getAccountFrom()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot request money from yourself!");
        Transfer pendingTransfer = null;
        try {
            pendingTransfer = transferDao.handleTransfer(transfer);
        } catch (DaoException | IllegalArgumentException | InsufficientFundsException | InvalidAccountException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return transferToRequestDto(pendingTransfer, sendingUser, user);
    }

    /**
     * Accept or reject a transfer requested by another user
     * @param id The transfer ID
     * @param transferAcceptanceDto A dto that initially contains a boolean transferAccepted that either
     *                              accepts or rejects the requested transfer
     * @param principal The logged in user
     * @return A TransferAcceptanceDto that includes usernames of account holders, the new balance of the user,
     * and the timestamp of the completion
     */
    @RequestMapping(path = "/requests/{id}", method = RequestMethod.POST)
    public TransferAcceptanceDto acceptTransfer(@PathVariable int id, @RequestBody TransferAcceptanceDto transferAcceptanceDto, Principal principal) {
        Transfer transfer = transferDao.getTransferById(id);
        BigDecimal moneyToSubtract = transfer.getAmount();
        BigDecimal currentBalance = accountDao.getPrimaryAccount(userDao.getUserByUsername(principal.getName())).getBalance();
        BigDecimal potentialBalance = currentBalance.subtract(moneyToSubtract);
        if (!(potentialBalance.compareTo(new BigDecimal("0.00")) > -1)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This would cause a negative account balance.");
        }
        String status = transferDao.getTransferStatusById(transfer.getTransferStatusId()).getTransferStatusDescription();
        if (transfer.getTransferCompleted() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("This request has already been %s.", status));
        User user = userDao.getUserByUsername(principal.getName());
        User receivingUser = accountDao.getUserOfAccount(accountDao.getAccountById(transfer.getAccountTo()));
        if (verifyTransferBelongToUser(transfer, principal, true)) {
            if (transferAcceptanceDto.isTransferAccepted()) {
                transfer.setTransferStatusId(transferDao.getTransferStatusByDesc("approved").getTransferStatusId());
            } else {
                transfer.setTransferStatusId(transferDao.getTransferStatusByDesc("rejected").getTransferStatusId());
            }
            Transfer completedTransfer = null;
            try {
                completedTransfer = transferDao.handleTransfer(transfer);
            } catch (DaoException | IllegalArgumentException | InsufficientFundsException | InvalidAccountException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            return transferToAcceptanceDto(completedTransfer, user, receivingUser, transferAcceptanceDto.isTransferAccepted());
        } else throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot change the status of someone else's requested transfer.");
    }

    /**
     * Verify that the transfer belongs to the user. The user must own one of the accounts listed in the transfer.
     * @param transfer The specific transfer
     * @param principal The logged in user
     * @param onlySender A boolean indicating the method to only look at the sender of the transfer
     * @return A boolean indicating if the transfer belongs to the user
     */
    private boolean verifyTransferBelongToUser(Transfer transfer, Principal principal, boolean onlySender) {
        User user = userDao.getUserByUsername(principal.getName());
        List<Account> usersAccountList = accountDao.getAccounts(user);
        boolean transferBelongsToUser = false;
        for (Account account : usersAccountList) {
            if (onlySender) {
                transferBelongsToUser = account.getAccountId() == transfer.getAccountFrom();
            } else {
                transferBelongsToUser = account.getAccountId() == transfer.getAccountTo() ||
                        account.getAccountId() == transfer.getAccountFrom();
            }
        }
        return transferBelongsToUser;
    }

    /**
     * Verify that the transfer belongs to the user. The user must own one of the accounts listed in the transfer.
     * @param transfer The specific transfer
     * @param principal The logged in user
     * @return A boolean indicating if the transfer belongs to the user
     */
    private boolean verifyTransferBelongToUser(Transfer transfer, Principal principal) {
        return verifyTransferBelongToUser(transfer, principal, false);
    }

    /**
     * Creates a TransferSendDto from a Transfer
     * @param transfer The specific transfer
     * @param sender The user that is sending the transfer
     * @param receiver The user receiving the transfer
     * @return The full TransferSendDto which adds usernames, user balance and a time completed
     */
    private TransferSendDto transferToSendDto(Transfer transfer, User sender, User receiver) {
        TransferSendDto transferSendDto = new TransferSendDto();
        transferSendDto.setTransferId(transfer.getTransferId());
        transferSendDto.setAccountId(transfer.getAccountFrom());
        transferSendDto.setSenderName(sender.getUsername());
        transferSendDto.setReceiverName(receiver.getUsername());
        transferSendDto.setAmountSent(transfer.getAmount());
        transferSendDto.setBalance(accountDao.getAccountById(transfer.getAccountFrom()).getBalance());
        transferSendDto.setTimeCompleted(transfer.getTransferCompleted());
        return transferSendDto;
    }

    /**
     * Creates a TransferRequestDto from a Transfer
     * @param transfer The specific transfer
     * @param sender The user that is being requested a transfer from
     * @param receiver The user receiving the transfer
     * @return The full TransferSendDto which adds usernames and a time created
     */
    private TransferRequestDto transferToRequestDto(Transfer transfer, User sender, User receiver) {
        TransferRequestDto transferRequestDto = new TransferRequestDto();
        transferRequestDto.setTransferId(transfer.getTransferId());
        transferRequestDto.setAccountId(transfer.getAccountTo());
        transferRequestDto.setPayerName(sender.getUsername());
        transferRequestDto.setRequesterName(receiver.getUsername());
        transferRequestDto.setAmountRequested(transfer.getAmount());
        transferRequestDto.setTimeCreated(transfer.getTransferCreated());
        return transferRequestDto;
    }

    /**
     * Creates a TransferAcceptanceDto from a transfer
     * @param transfer The specific transfer
     * @param sender The user that the transfer was requested from
     * @param receiver The user receiving the transfer
     * @param isAccepted A boolean either accepting or rejecting the request
     * @return The full TransferAcceptanceDto which includes an updated Status indicating the acceptance
     * of the transfer. Also includes the user's new balance and the time completed.
     */
    private TransferAcceptanceDto transferToAcceptanceDto(Transfer transfer, User sender, User receiver, boolean isAccepted) {
        TransferAcceptanceDto transferAcceptanceDto = new TransferAcceptanceDto();
        transferAcceptanceDto.setTransferAccepted(isAccepted);
        transferAcceptanceDto.setTransferId(transfer.getTransferId());
        transferAcceptanceDto.setAccountId(transfer.getAccountFrom());
        transferAcceptanceDto.setSenderName(sender.getUsername());
        transferAcceptanceDto.setReceiverName(receiver.getUsername());
        transferAcceptanceDto.setAmountSent(transfer.getAmount());
        transferAcceptanceDto.setBalance(accountDao.getAccountById(transfer.getAccountFrom()).getBalance());
        transferAcceptanceDto.setTimeCompleted(transfer.getTransferCompleted());
        return transferAcceptanceDto;
    }

    /**
     * Creates a TransferDto from a transfer
     * @param transfer The specific transfer
     * @return A TransferDto which adds the type/status descriptions and the account's usernames to a transfer.
     */
    private TransferDto transferToDto(Transfer transfer) {
        TransferDto transferDto = new TransferDto();
        transferDto.setTransferId(transfer.getTransferId());
        transferDto.setTypeId(transfer.getTypeId());
        transferDto.setType(transferDao.getTransferTypeById(transfer.getTypeId()).getTransferTypeDescription());
        transferDto.setTransferStatusId(transfer.getTransferStatusId());
        transferDto.setStatus(transferDao.getTransferStatusById(transfer.getTransferStatusId()).getTransferStatusDescription());
        transferDto.setAccountFrom(transfer.getAccountFrom());
        transferDto.setFromUsername(accountDao.getUserOfAccount(accountDao.getAccountById(transfer.getAccountFrom())).getUsername());
        transferDto.setAccountTo(transfer.getAccountTo());
        transferDto.setToUsername(accountDao.getUserOfAccount(accountDao.getAccountById(transfer.getAccountTo())).getUsername());
        transferDto.setAmount(transfer.getAmount());
        transferDto.setTransferCreated(transfer.getTransferCreated());
        transferDto.setTransferCompleted(transfer.getTransferCompleted());
        return transferDto;
    }
}
