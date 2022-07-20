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
import tech.vegapay.commons.dto.LedgerEntryDto;
import tech.vegapay.commons.enums.LedgerApi;
import tech.vegapay.commons.utils.InternalClientUtil;

@Slf4j
@Service
public class LedgerHandler implements Ledger {

    @Autowired
    private InternalClientUtil internalClientUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.ledger.service.base.url}")
    String baseUrl;

    @Override
    public LedgerEntryDto createLedgerEntry(LedgerEntryDto ledgerdto) {
        HttpEntity httpEntity = internalClientUtil.getClientBuilder()
                .setAccept()
                .setRequest(ledgerdto)
                .setContentType(MediaType.APPLICATION_JSON)
                .build();

        try {
            final String url = baseUrl + LedgerApi.POST_LEDGER.getUrl();

            ResponseEntity<LedgerEntryDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, LedgerEntryDto.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Exception occur during create LedgerEntry for transactionId {}", ledgerdto.getTransactionId(), ex);
        }

        return null;
    }
}
