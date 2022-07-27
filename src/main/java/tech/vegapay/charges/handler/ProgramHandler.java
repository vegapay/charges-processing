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
import tech.vegapay.commons.dto.policies.AllPolicies;
import tech.vegapay.commons.enums.PolicyApi;
import tech.vegapay.commons.utils.InternalClientUtil;

@Service
@Slf4j
public class ProgramHandler implements Program {

    @Autowired
    InternalClientUtil internalClientUtil;

    @Value("${api.program.service.base.url}")
    String baseUrl;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public AllPolicies getProgramPolicy(String programId) {
        log.info("Getting AllPolicies for Program id : {}", programId);
        HttpEntity httpEntity = internalClientUtil.getClientBuilder().setContentType(MediaType.APPLICATION_JSON).setAccept().build();

        try {

            final String url = baseUrl + PolicyApi.GET_POLICY.getUrl().replace("{programId}", programId);
            log.info("url : {}", url);
            ResponseEntity<AllPolicies> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, AllPolicies.class);
            log.info("responseEntity : {}", responseEntity);
            if (responseEntity.getStatusCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseEntity.getBody())) {
                return responseEntity.getBody();
            }

        } catch (Exception ex) {
            log.error("Error while getting All policies for programId : {}", programId, ex);
        }
        return null;
    }
}
