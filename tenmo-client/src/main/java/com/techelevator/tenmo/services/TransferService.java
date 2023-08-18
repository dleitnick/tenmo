package com.techelevator.tenmo.services;

import com.techelevator.exception.ApiException;
import com.techelevator.tenmo.model.transfer.TransferAcceptanceDto;
import com.techelevator.tenmo.model.transfer.TransferDto;
import com.techelevator.tenmo.model.transfer.TransferRequestDto;
import com.techelevator.tenmo.model.transfer.TransferSendDto;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class TransferService {

    private final String BASE_URL;
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public TransferService(String url, String authToken) {
        this.BASE_URL = url;
        this.authToken = authToken;
    }

    public TransferDto getTransfer(int id) {
        TransferDto transfer = null;
        try {
            ResponseEntity<TransferDto> response =
                    restTemplate.exchange(BASE_URL + "transfers/" + id, HttpMethod.GET, makeAuthEntity(), TransferDto.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    public TransferDto[] getAllTransfers() {
        TransferDto[] transfers = null;
        try {
            ResponseEntity<TransferDto[]> response =
                    restTemplate.exchange(BASE_URL + "transfers", HttpMethod.GET, makeAuthEntity(), TransferDto[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public TransferDto[] getPendingTransfers() {
        int pendingTransfer = 1;
        TransferDto[] transfers = null;
        try {
            ResponseEntity<TransferDto[]> response =
                    restTemplate.exchange(BASE_URL + "transfers?statusId=" + pendingTransfer, HttpMethod.GET, makeAuthEntity(), TransferDto[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public TransferAcceptanceDto approveOrReject(int id, boolean approved) {
        TransferAcceptanceDto transfer = new TransferAcceptanceDto();
        transfer.setTransferAccepted(approved);
        try {
            ResponseEntity<TransferAcceptanceDto> response =
                    restTemplate.exchange(BASE_URL + "requests/" + id, HttpMethod.POST, makeTransferDtoEntity(transfer), TransferAcceptanceDto.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            throw new ApiException(e.getMessage());
        }
        return transfer;
    }

    public TransferSendDto send(String receiverName, BigDecimal amountToSend) {
        return moveBetweenAccounts(receiverName, amountToSend, 0);
    }

    public TransferSendDto moveBetweenAccounts(String receiverName, BigDecimal amountToSend, int accountId) {
        TransferSendDto transfer = new TransferSendDto();
        transfer.setReceiverName(receiverName);
        transfer.setAmountSent(amountToSend);
        if (accountId > 0) {
            transfer.setAccountId(accountId);
        }
        try {
            ResponseEntity<TransferSendDto> response =
                    restTemplate.exchange(BASE_URL + "send", HttpMethod.POST, makeTransferDtoEntity(transfer), TransferSendDto.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            throw new ApiException(e.getMessage());
        }
        return transfer;
    }

    public TransferRequestDto request(String payerName, BigDecimal amountRequested) {
        TransferRequestDto transfer = new TransferRequestDto();
        transfer.setPayerName(payerName);
        transfer.setAmountRequested(amountRequested);
        try {
            ResponseEntity<TransferRequestDto> response =
                    restTemplate.exchange(BASE_URL + "requests", HttpMethod.POST, makeTransferDtoEntity(transfer), TransferRequestDto.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    private <T>HttpEntity<T> makeTransferDtoEntity(T transferDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transferDto, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }


}
