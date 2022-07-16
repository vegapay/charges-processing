package tech.vegapay.charges.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import tech.vegapay.charges.service.ChargeProcessingService;
import tech.vegapay.commons.utils.MapperUtil;

import java.util.HashMap;

@Slf4j
public class ProgramChargeProcessingConsumer {

    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private ChargeProcessingService chargeProcessingService;


    @KafkaListener(topics = "${kafka.topic.charge.program.id}", groupId = "charge-processing-group", autoStartup = "true")
    public void chargeProcessingForProgramId(@Payload String msg, Acknowledgment acknowledgment) {
        log.info("Received message for chargeProcessingByBillId: {}", msg);

        acknowledgment.acknowledge();
        try {
            HashMap map = mapperUtil.convertValue(msg, HashMap.class);

            String programId = String.valueOf(map.get("programId"));
            String eventType = String.valueOf(map.get("eventType"));

            chargeProcessingService.processBillForProgramId(programId, eventType);
        } catch (Exception e) {
            log.error("Error in chargeProcessingByBillId for account id {} : {}", msg, e.getMessage());
        }

    }

}
