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
import tech.vegapay.commons.dto.ChargeDto;
import tech.vegapay.commons.enums.ChargeApi;
import tech.vegapay.commons.utils.InternalClientUtil;

@Slf4j
@Service
public class ChargeHandler implements Charge {

    @Autowired
    private InternalClientUtil internalClientUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.charge.service.base.url}")
    String baseUrl;

    /**
     * @param chargeDto 
     * @return
     */
    @Override
    public ChargeDto createCharge(ChargeDto chargeDto) {

        HttpEntity httpEntity = internalClientUtil.getClientBuilder()
                .setAccept()
                .setRequest(chargeDto)
                .setContentType(MediaType.APPLICATION_JSON)
                .build();

        try {
            final String url = baseUrl + ChargeApi.CREATE_CHARGE.getUrl();

            ResponseEntity<ChargeDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ChargeDto.class);

            if(responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())){
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Exception occur during create Charge for accountId {}", chargeDto.getAccountId(), ex);
        }

        return null;
    }

    /**
     * @param chargeId 
     * @return
     */
    @Override
    public ChargeDto getChargeById(String chargeId) {
        return null;
    }
}
