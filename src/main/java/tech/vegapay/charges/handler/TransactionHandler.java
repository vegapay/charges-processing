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
import tech.vegapay.commons.dto.TransactionDto;
import tech.vegapay.commons.enums.TransactionApi;
import tech.vegapay.commons.utils.InternalClientUtil;

@Slf4j
@Service
public class TransactionHandler implements Transaction {

    @Autowired
    private InternalClientUtil internalClientUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.ledger.service.base.url}")
    String baseUrl;

    @Override
    public TransactionDto createTransaction(TransactionDto transactionDto) {
        HttpEntity httpEntity = internalClientUtil.getClientBuilder()
                .setAccept()
                .setRequest(transactionDto)
                .setContentType(MediaType.APPLICATION_JSON)
                .build();

        try {
            final String url = baseUrl + TransactionApi.POST_TRANSACTION.getUrl();

            ResponseEntity<TransactionDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TransactionDto.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Exception occur during create Transaction for accoutId {}", transactionDto.getAccountId(), ex);
        }

        return null;
    }

    @Override
    public TransactionDto getTransaction(String transactionId) {
        log.info("Getting Transaction Details for Transaction id : {}", transactionId);
        HttpEntity httpEntity = internalClientUtil.getClientBuilder().setContentType(MediaType.APPLICATION_JSON).setAccept().build();

        try {

            final String url = baseUrl + TransactionApi.GET_TRANSACTION_BY_ID.getUrl().replace("{transactionId}", transactionId);
            log.info("url : {}", url);
            ResponseEntity<TransactionDto> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, TransactionDto.class);
            log.info("responseEntity : {}", responseEntity);
            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Error while getting Bill Details for billId id : {}", transactionId, ex);
        }
        return null;
    }
}
