package tech.vegapay.charges.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import tech.vegapay.commons.dto.AccountDto;
import tech.vegapay.commons.dto.LockDto;
import tech.vegapay.commons.dto.TransactionDto;
import tech.vegapay.commons.enums.AccountApi;
import tech.vegapay.commons.enums.LockApi;
import tech.vegapay.commons.enums.TransactionApi;
import tech.vegapay.commons.utils.InternalClientUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AccountHandler implements Account {

    @Autowired
    InternalClientUtil internalClientUtil;

    @Value("${api.account.service.base.url}")
    String baseUrl;

    @Value("${api.transaction.service.base.url}")
    String transactionBaseUrl;

    @Autowired
    RestTemplate restTemplate;


    @Override
    public List<AccountDto> getAllAccountsByProgramId(String programId) {

        log.info("Getting account for program id {}", programId);
        HttpEntity request = internalClientUtil.getClientBuilder().setContentType(MediaType.APPLICATION_JSON).setAccept().build();

        try {
            final String url = baseUrl + AccountApi.GET_ALL_ACCOUNTS_BY_PROGRAM_ID.getPath() + programId;

            ResponseEntity<AccountDto[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, AccountDto[].class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return Arrays.asList(responseEntity.getBody());
            }

        } catch (Exception ex) {
            log.error("Error while getting account for program id {}", programId, ex);
        }
        return null;

    }

    @Override
    public AccountDto getAccount(String accountId) {

        log.info("Getting account for account id {}", accountId);
        HttpEntity request = internalClientUtil.getClientBuilder().setContentType(MediaType.APPLICATION_JSON).setAccept().build();

        try {
            Map<String, String> urlParams = new HashMap<>();
            urlParams.put("accountId", accountId);

            final String url = baseUrl + AccountApi.GET_ACCOUNT.getPath() + accountId;
            log.info("final url {}", url);

            ResponseEntity<AccountDto> response = restTemplate.exchange(url, HttpMethod.GET, request, AccountDto.class);

            if (response.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(response.getBody())) {
                return response.getBody();
            }

        } catch (Exception ex) {
            log.error("Error while getting account for account id {}", accountId, ex);
            return null;
        }
        return null;
    }

    @Override
    public LockDto createLockonAccount(String accountId) {
        HttpEntity httpEntity = internalClientUtil.getClientBuilder()
                .setAccept()
                .setContentType(MediaType.APPLICATION_JSON)
                .build();

        try {
            final String url = transactionBaseUrl + LockApi.CREATE_LOCK.getUrl() + accountId;

            ResponseEntity<LockDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, LockDto.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Exception occur during lock acquiring for accoutId {}", accountId, ex);
        }

        return null;
    }

    @Override
    public String releaseAccountLock(String accountId, String token) {
        HttpEntity httpEntity = internalClientUtil.getClientBuilder()
                .setAccept()
                .setRequest("token", token)
                .setContentType(MediaType.APPLICATION_JSON)
                .build();

        try {
            final String url = baseUrl + LockApi.RELEASE_LOCK.getUrl() + accountId;

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Exception occur during relesing Lock for accoutId {}", accountId, ex);
        }

        return null;
    }
}
