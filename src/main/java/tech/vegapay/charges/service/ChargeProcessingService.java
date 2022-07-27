package tech.vegapay.charges.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import tech.vegapay.charges.dto.ChargesComputeRequest;
import tech.vegapay.charges.handler.*;
import tech.vegapay.commons.dto.*;
import tech.vegapay.commons.dto.policies.AllPolicies;
import tech.vegapay.commons.dto.policies.BNPLPolicy;
import tech.vegapay.commons.dto.policies.charges.*;
import tech.vegapay.commons.dto.policies.charges.card.TransactionCharge;
import tech.vegapay.commons.enums.TransactionEnum;
import tech.vegapay.commons.utils.ChargeTransformation;

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
    private Ledger ledger;

    @Autowired
    private Transaction transaction;

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

        long charges = getCharges(
                ChargesComputeRequest
                        .builder()
                        .billId(billId)
                        .programId(accountDto.getProgramId())
                        .eventType(chargeCategory)
                        .build()
        );

        ChargeDto chargeDto = ChargeDto.builder()
                .id(UUID.randomUUID().toString())
                .chargeId(UUID.randomUUID().toString())
                .chargeAmount(charges)
                .dueCharge(charges)
                .paidCharge(0L)
                .status(ChargeStatus.UNPAID)
                .accountId(tempBill.getAccountId())
                .description("Charge For " + chargeCategory)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        chargeDto = charge.createCharge(chargeDto);

        if (chargeDto.getId() != null) {
            log.info("Charge Created Successfully for account Id {}", tempBill.getAccountId());
        }
    }

    public long getCharges(ChargesComputeRequest chargesComputeRequest) {
        //we need to fetch charges file/json using programId

        AllPolicies allPolicies = program.getProgramPolicy(chargesComputeRequest.getProgramId());
        ChargePolicy chargePolicy = allPolicies.getChargePolicy().get();
        log.info("Charge policy: {}", chargePolicy);
        BNPLPolicy bnplPolicy = allPolicies.getBNPLPolicy().get();
        log.info("BNPL policy: {}", bnplPolicy);
        long value = 0;
        switch (chargesComputeRequest.getEventType()) {
            //todo :: fix start date here..
            case BILL_DATE_TO_DUE_DATE:
                value = computeCharges(chargePolicy.getChargeRules(), chargesComputeRequest.getEventType(), bnplPolicy.getBillGenerationDate(), chargesComputeRequest.getBillId());
                break;
            case DUE_DATE_TO_HARD_BLOCK:
                value = computeCharges(chargePolicy.getChargeRules(), chargesComputeRequest.getEventType(), bnplPolicy.getDueDate(), chargesComputeRequest.getBillId());
                break;
            case HARD_BLOCK_TO_PERMANENT_BLOCK:
                value = computeCharges(chargePolicy.getChargeRules(), chargesComputeRequest.getEventType(), bnplPolicy.getHardDueDate(), chargesComputeRequest.getBillId());
                break;
            case TRANSACTION:
                value = computeTransactionCharges(chargePolicy.getChargeRules(), chargesComputeRequest.getTransactionId());
                break;
            default:
        }

        return value;
    }

    private long computeCharges(ChargeRule[] chargeRules, ChargeCategory category, int date, String billId) {
        BillDto tempBill = bill.getBill(billId);

        // for testing changing bill due date;
        tempBill.setBillDate(new Date("2022/06/17 00:00:00"));
        double billAmount = tempBill.getBillAmount();
        Date startDate = calculateDate(tempBill, date);

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

    private long computeTransactionCharges(ChargeRule[] chargeRules, String transactionId) {

        long value = 0L;
        for (ChargeRule temp : chargeRules) {
            if (temp.getChargeCategory().equals(ChargeCategory.TRANSACTION)) {
                PayoutMechanismCharge payoutMechanismCharge = temp.getPayoutMechanismCharge();
                TransactionDto transactionDto = transaction.getTransaction(transactionId);
                TransactionEnum.TransactionSubType subType = transactionDto.getTransactionSubtype();
                log.info("Computing charges for transaction subtype: {}", subType);
                switch (subType) {
                    //todo :: fix start date here..
                    case ECOM:
                        value = calculateCharges(payoutMechanismCharge.getCardCharges().getEcommerce(), transactionDto);
                        break;
                    case POS:
                        value = calculateCharges(payoutMechanismCharge.getCardCharges().getPos(), transactionDto);
                        break;
                    case CONTACTLESS:
                        value = calculateCharges(payoutMechanismCharge.getCardCharges().getContactLess(), transactionDto);
                        break;
                    case ATM:
                        value = calculateCharges(payoutMechanismCharge.getCardCharges().getAtm(), transactionDto);
                        break;
                    case UPI:
                        value = calculateCharges(payoutMechanismCharge.getUpi(), transactionDto);
                        break;
                    case BANK_TRANSFER:
                        value = calculateCharges(payoutMechanismCharge.getBankTransfer(), transactionDto);
                        break;
                    case GV:
                        value = calculateCharges(payoutMechanismCharge.getGv(), transactionDto);
                        break;
                    default:
                        value = 0L;
                        break;
                }
            }
        }
        log.info("Computing charges for transaction value {}", value);
        return value;
    }

    private long calculateCharges(TransactionCharge transactionCharge, TransactionDto transactionDto) {
        log.info("Computing charges for transaction", transactionCharge.toString());
        if (transactionCharge.getChargeType().equals(ChargeType.AMOUNT)) {
            double tempCharge = transactionCharge.getValue();
            if (!transactionCharge.isInclusiveOfGst()) {
                tempCharge += tempCharge * transactionCharge.getGstTax() / 100;
            }
            double tempDouble = tempCharge * 100;
            return Double.valueOf(tempDouble).longValue();
        } else if (transactionCharge.getChargeType().equals(ChargeType.PERCENTAGE)) {
            double tempCharge = transactionDto.getAmount() * transactionCharge.getValue() / 100;
            if (!transactionCharge.isInclusiveOfGst()) {
                tempCharge += tempCharge * transactionCharge.getGstTax() / 100;
            }
            double tempDouble = tempCharge * 100;
            return Double.valueOf(tempDouble).longValue();
        } else {
            //todo :: add exception here
            return 0;
        }
    }

    private long getEstimatedCharges(Date startDate, double billAmount, AbstractCharges charges) {
        Date currentDate = new Date();
        long days = getDifferenceDays(startDate, currentDate);
        if (charges.getChargeType().equals(ChargeType.AMOUNT)) {
            double tempCharge = charges.getValue() * days;
            if (!charges.isInclusiveOfGst()) {
                tempCharge += tempCharge * charges.getGstTax() / 100;
            }
            double tempDouble = tempCharge * 100;
            return Double.valueOf(tempDouble).longValue();
        } else if (charges.getChargeType().equals(ChargeType.PERCENTAGE)) {
            double tempCharge = billAmount * charges.getValue() * days / (365 * 100);
            if (!charges.isInclusiveOfGst()) {
                tempCharge += tempCharge * charges.getGstTax() / 100;
            }
            double tempDouble = tempCharge * 100;
            return Double.valueOf(tempDouble).longValue();
        } else {
            //todo :: add exception here
            return 0;
        }
    }

    public Date calculateDate(BillDto billDto, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billDto.getBillDate());
        calendar.set(Calendar.DATE, date);

        return calendar.getTime();
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
        List<AccountDto> accountDtoList = account.getAllAccountsByProgramId(programId);

        if (accountDtoList == null) {
            log.error("No accounts found for programId : {}", programId);
            return;
        }

        accountDtoList.forEach(it -> {
            try {
                // find bill for account ids
                BillDto billDto = bill.getLatestBill(it.getId());
                if (billDto == null) {
                    log.error("No bill found for accountId : {}", it.getId());
                    return;
                }

                // filter bills based on status
                if (Arrays.asList(BillStatus.UNPAID, BillStatus.PARTIAL_PAID).contains(billDto.getStatus())) {
                    HashMap<String, Object> map = new HashMap<String, Object>() {{
                        put("billId", billDto.getId());
                        put("eventType", eventType);
                    }};

                    // Push the filtered bills id to kafka with eventType
                    kafkaTemplate.send(kafkaChargeProcessingTopicByBillId, billDto.getId().toString(), map);
                }
            } catch (Exception e) {
                log.error("Error while processing bill for accountId : {}", it.getId());
            }
        });

    }

    public String applyTransactionCharges(ChargesComputeRequest chargesComputeRequest) {

        TransactionDto parentTransaction = transaction.getTransaction(chargesComputeRequest.getTransactionId());

        long charges = getCharges(
                ChargesComputeRequest
                        .builder()
                        .transactionId(chargesComputeRequest.getTransactionId())
                        .programId(chargesComputeRequest.getProgramId())
                        .eventType(ChargeCategory.TRANSACTION)
                        .build()
        );

        ChargeDto chargeDto = ChargeDto.builder()
                .id(UUID.randomUUID().toString())
                .chargeId(UUID.randomUUID().toString())
                .chargeAmount(charges)
                .dueCharge(charges)
                .paidCharge(0L)
                .status(ChargeStatus.UNPAID)
                .accountId(parentTransaction.getAccountId())
                .description("Charge For Transaction with Id : " + parentTransaction.getId().toString())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        chargeDto = charge.createCharge(chargeDto);


        if (chargeDto.getId() != null) {
            log.info("Charge Created Successfully for transaction Id {}", chargesComputeRequest.getTransactionId());
        }

        TransactionDto transactionDto = ChargeTransformation.toTransaction(chargeDto, parentTransaction);

        LockDto lockDto = account.createLockonAccount(parentTransaction.getAccountId());
        AccountDto accountDto = account.getAccount(parentTransaction.getAccountId());
        long availableLimit = accountDto.getAvailableLimit();
        long availableLimitAfterTransaction = availableLimit - charges;
        //todo :: update account available limit..
        transactionDto.setAfterTransactionBalance(availableLimitAfterTransaction);
        transaction.createTransaction(transactionDto);

        account.releaseAccountLock(parentTransaction.getAccountId(), lockDto.getToken());

        LedgerEntryDto ledgerEntryDto = ChargeTransformation.toLedgerEntry(transactionDto);
        ledgerEntryDto.setAfterTransactionBalance(availableLimitAfterTransaction);
        ledger.createLedgerEntry(ledgerEntryDto);

        return "";
    }
}
