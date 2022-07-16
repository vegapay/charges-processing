package tech.vegapay.charges.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import tech.vegapay.charges.dto.ChargesComputeRequest;
import tech.vegapay.charges.handler.Account;
import tech.vegapay.charges.handler.Bill;
import tech.vegapay.charges.handler.Charge;
import tech.vegapay.charges.handler.Program;
import tech.vegapay.commons.dto.AccountDto;
import tech.vegapay.commons.dto.BillDto;
import tech.vegapay.commons.dto.BillStatus;
import tech.vegapay.commons.dto.ChargeDto;
import tech.vegapay.commons.dto.policies.AllPolicies;
import tech.vegapay.commons.dto.policies.BNPLPolicy;
import tech.vegapay.commons.dto.policies.charges.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ChargeProcessingService {

    @Autowired
    private Bill bill;

    @Autowired
    private Program program;

    @Autowired
    private Account account;

    @Autowired
    private Charge charge;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.charge.by.bill.id}")
    String kafkaChargeProcessingTopicByBillId;

    public void chargeProcessingByBillId(String billId, ChargeCategory chargeCategory) {

        Assert.notNull(billId, "bill Id can not be null");
        Assert.notNull(chargeCategory, "ChargeCategory Can not be null");

        BillDto tempBill = bill.getBill(billId);
        AccountDto accountDto = account.getAccount(tempBill.getAccountId());

        assert accountDto != null;

        Double charges = getCharges(
                ChargesComputeRequest
                        .builder()
                        .billId(billId)
                        .programId(accountDto.getProgramId())
                        .eventType(chargeCategory)
                        .build()
        );

        ChargeDto chargeDto = ChargeDto.builder()
                .id(UUID.randomUUID())
                .chargeId(UUID.randomUUID().toString())
                .chargeAmount(charges.longValue())
                .accountId(tempBill.getAccountId())
                .description("Charge For " + chargeCategory)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        chargeDto = charge.createCharge(chargeDto);

        if(charges != null && chargeDto.getId() != null){
            log.info("Charge Created Successfully for account Id {}", tempBill.getAccountId());
        }
    }

    public Double getCharges(ChargesComputeRequest charges) {
        //we need to fetch charges file/json using programId

        AllPolicies allPolicies = program.getProgramPolicy(charges.getProgramId());
        ChargePolicy chargePolicy = allPolicies.getChargePolicy();
        BNPLPolicy bnplPolicy = allPolicies.getBnplPolicy();
        double value = 0;
        BillDto tempBill = bill.getBill(charges.getBillId());

        switch (charges.getEventType()) {
            //todo :: fix start date here..
            case BILL_DATE_TO_DUE_DATE:
                value = computeCharges(chargePolicy.getChargeRules(), charges.getEventType(), calculateDate(tempBill, bnplPolicy.getBillDate()), tempBill.getBillAmount());
                break;
            case DUE_DATE_TO_HARD_BLOCK:
                value = computeCharges(chargePolicy.getChargeRules(), charges.getEventType(), calculateDate(tempBill, bnplPolicy.getDueDate()), tempBill.getBillAmount());
                break;
            case HARD_BLOCK_TO_PERMANENT_BLOCK:
                value = computeCharges(chargePolicy.getChargeRules(), charges.getEventType(), calculateDate(tempBill, bnplPolicy.getHardDueDate()), tempBill.getBillAmount());
                break;
            default:
                value = 10;
        }

        return value;
    }

    public Date calculateDate(BillDto billDto, String date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billDto.getBillDate());
        calendar.set(Calendar.DATE, Integer.parseInt(date));

        return calendar.getTime();
    }

    private double computeCharges(ChargeRule[] chargeRules, ChargeCategory category, Date startDate, double billAmount) {
        for (ChargeRule temp : chargeRules) {
            if (temp.getChargeCategory().equals(category)) {
                PerDayCharge perDayCharge = temp.getPerDayCharge();
                if (perDayCharge != null) {
                    return getEstimatedCharges(startDate, billAmount, perDayCharge);
                } else {
                    SlabCharge[] slabCharges = temp.getSlabCharges();
                    for (SlabCharge tempSlabCharge : slabCharges) {
                        if (Double.compare(tempSlabCharge.getStartAmount(), billAmount) == -1 && Double.compare(billAmount, tempSlabCharge.getEndAmount()) == -1) {
                            return getEstimatedCharges(startDate, billAmount, tempSlabCharge);
                        }
                    }
                }
            }
        }
        //todo :: add exception here
        return 0;
    }

    private double getEstimatedCharges(Date startDate, double billAmount, AbstractCharges charges) {
        Date currentDate = new Date();
        long days = getDifferenceDays(startDate, currentDate);
        if (charges.getChargeType().equals(ChargeType.AMOUNT)) {
            double tempCharge = charges.getValue() * days;
            if (!charges.isInclusiveOfGst()) {
                tempCharge += tempCharge * charges.getGstTax() / 100;
            }
            return tempCharge;
        } else if (charges.getChargeType().equals(ChargeType.PERCENTAGE)) {
            double tempCharge = billAmount * charges.getValue() * days / (365 * 100);
            if (!charges.isInclusiveOfGst()) {
                tempCharge += tempCharge * charges.getGstTax() / 100;
            }
            return tempCharge;
        } else {
            //todo :: add exception here
            return 0;
        }
    }


    public static long getDifferenceDays(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }


    public void processBillForProgramId(String programId, String eventType) {

        log.info("ProcessingCharges for programId : {} and eventType : {}", programId, eventType);

        Assert.notNull(programId, "programId cannot be null");
        Assert.notNull(eventType, "eventType cannot be null");

        //find all accountIds for programId
        List<AccountDto> accountDtoList =  account.getAllAccountsByProgramId(programId);

        if (accountDtoList == null){
            log.error("No accounts found for programId : {}", programId);
            return;
        }

        accountDtoList.forEach(it -> {
            try {
                // find bill for account ids
                BillDto billDto = bill.getLatestBill(it.getAccountId());
                if (billDto == null){
                    log.error("No bill found for accountId : {}", it.getAccountId());
                    return;
                }

                // filter bills based on status
                if(Arrays.asList(BillStatus.UNPAID, BillStatus.PARTIAL_PAID).contains(billDto.getStatus())){
                    HashMap<String, Object> map = new HashMap<String, Object>() {{
                        put("billId", billDto.getId());
                        put("eventType", eventType);
                    }};

                    // Push the filtered bills id to kafka with eventType
                    kafkaTemplate.send(kafkaChargeProcessingTopicByBillId, billDto.getId().toString(),map);
                }
            } catch (Exception e) {
                log.error("Error while processing bill for accountId : {}", it.getAccountId());
            }
        });

    }
}
