package tech.vegapay.charges.handler;

import tech.vegapay.commons.dto.AccountDto;
import tech.vegapay.commons.dto.LockDto;

import java.util.List;

public interface Account {

    public List<AccountDto> getAllAccountsByProgramId(String programId);

    AccountDto getAccount(String accountId);

    LockDto createLockonAccount(String accountId);

    String releaseAccountLock(String accountId, String token);
}
