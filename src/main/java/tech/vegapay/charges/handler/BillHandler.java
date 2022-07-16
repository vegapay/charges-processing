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
import tech.vegapay.commons.dto.BillDto;
import tech.vegapay.commons.enums.BillingApi;
import tech.vegapay.commons.utils.InternalClientUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class BillHandler implements Bill {

    @Autowired
    InternalClientUtil internalClientUtil;

    @Value("${api.bill.service.base.url}")
    String baseUrl;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public BillDto getBill(String billId) {
        log.info("Getting Bill Details for Bill id : {}", billId);
        HttpEntity httpEntity = internalClientUtil.getClientBuilder().setContentType(MediaType.APPLICATION_JSON).setAccept().build();

        try {

            final String url = baseUrl + BillingApi.GET_BILL_BY_ID.getUrl().replace("{billId}", billId);
            log.info("url : {}", url);
            ResponseEntity<BillDto> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, BillDto.class);
            log.info("responseEntity : {}", responseEntity);
            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Error while getting Bill Details for billId id : {}", billId, ex);
        }
        return null;
    }

    @Override
    public List<BillDto> getBillByAccountId(String accountId) {

        log.info("Getting Bill Details for account id : {}", accountId);
        HttpEntity httpEntity = internalClientUtil.getClientBuilder().setContentType(MediaType.APPLICATION_JSON).setAccept().build();

        try {

            final String url = baseUrl + BillingApi.GET_BILL.getUrl().replace("{accountId}", accountId);
            log.info("url : {}", url);
            ResponseEntity<BillDto[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, BillDto[].class);
            log.info("responseEntity : {}", responseEntity);
            if(responseEntity.getStatusCode().is2xxSuccessful() && !Objects.isNull(responseEntity.getBody())) {
                return Arrays.asList(responseEntity.getBody());
            }

        } catch (Exception ex){
            log.error("Error while getting Bill Details for account id : {}", accountId, ex);
        }
        return null;
    }

    @Override
    public BillDto getLatestBill(String accountId) {

        log.info("Getting Bill Details for account id : {}", accountId);
        HttpEntity httpEntity = internalClientUtil.getClientBuilder().setContentType(MediaType.APPLICATION_JSON).setAccept().build();

        try {

            final String url = baseUrl + BillingApi.GET_LATEST_BILL_BY_ACCOUNT_ID.getUrl().replace("{accountId}", accountId);
            log.info("url : {}", url);
            ResponseEntity<BillDto> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, BillDto.class);
            log.info("responseEntity : {}", responseEntity);
            if(responseEntity.getStatusCode().is2xxSuccessful()) {
                if(!Objects.isNull(responseEntity.getBody())) {
                    return responseEntity.getBody();
                } else {
                    return new BillDto();
                }
            }

        } catch (Exception ex){
            log.error("Error while getting Bill Details for account id : {}", accountId, ex);
        }
        return null;
    }

}
