package tech.vegapay.charges.handler;

import tech.vegapay.commons.dto.TransactionDto;

public interface Transaction {

    TransactionDto createTransaction(TransactionDto transactionDto);

    TransactionDto getTransaction(String transactionId);
}
