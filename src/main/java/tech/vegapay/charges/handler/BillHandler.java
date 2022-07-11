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
import tech.vegapay.commons.dto.BillingDto;
import tech.vegapay.commons.enums.BillingApi;
import tech.vegapay.commons.utils.InternalClientUtil;

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
    public BillingDto getBill(String billId) {
        log.info("Getting Bill Details for Bill id : {}", billId);
        HttpEntity httpEntity = internalClientUtil.getClientBuilder().setContentType(MediaType.APPLICATION_JSON).setAccept().build();

        try {

            final String url = baseUrl + BillingApi.GET_BILL_BY_ID.getUrl().replace("{billId}", billId);
            log.info("url : {}", url);
            ResponseEntity<BillingDto> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, BillingDto.class);
            log.info("responseEntity : {}", responseEntity);
            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Error while getting Bill Details for billId id : {}", billId, ex);
        }
        return null;
    }
}
