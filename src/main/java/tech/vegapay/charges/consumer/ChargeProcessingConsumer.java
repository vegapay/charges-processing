package tech.vegapay.charges.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import tech.vegapay.charges.service.ChargeProcessingService;
import tech.vegapay.commons.dto.policies.charges.ChargeCategory;
import tech.vegapay.commons.utils.MapperUtil;

import java.util.HashMap;

@Slf4j
public class ChargeProcessingConsumer {

    @Autowired
    private ChargeProcessingService chargeProcessingService;

    @Autowired
    private MapperUtil mapperUtil;


    @KafkaListener(topics = "${kafka.topic.charge.by.bill.id}", groupId = "charge-processing-group", autoStartup = "true")
    public void chargeProcessingByBillId(@Payload String msg, Acknowledgment acknowledgment) {
        log.info("Received message for chargeProcessingByBillId: {}", msg);

        acknowledgment.acknowledge();
        try {
            HashMap map = mapperUtil.convertValue(msg, HashMap.class);

            String billId = String.valueOf(map.get("billId"));
            ChargeCategory chargeCategory = ChargeCategory.valueOf(String.valueOf(map.get("eventType")));

            chargeProcessingService.chargeProcessingByBillId(billId, chargeCategory);
        } catch (Exception e) {
            log.error("Error in chargeProcessingByBillId for account id {} : {}", msg, e.getMessage());
        }

    }

}
