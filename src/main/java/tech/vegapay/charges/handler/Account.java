package tech.vegapay.charges.handler;

import tech.vegapay.commons.dto.AccountDto;

import java.util.List;

public interface Account {

    public List<AccountDto> getAllAccountsByProgramId(String programId);

    AccountDto getAccount(String accountId);
}
