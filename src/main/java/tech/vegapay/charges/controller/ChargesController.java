package tech.vegapay.charges.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.vegapay.charges.entity.ChargesComputeRequest;
import tech.vegapay.charges.handler.Bill;
import tech.vegapay.charges.handler.Program;
import tech.vegapay.commons.dto.BillingDto;
import tech.vegapay.commons.dto.policies.AllPolicies;
import tech.vegapay.commons.dto.policies.BNPLPolicy;
import tech.vegapay.commons.dto.policies.charges.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@CrossOrigin()
@RestController()
@RequestMapping("/chargesProcessing")
public class ChargesController {

    @Autowired
    Bill bill;

    @Autowired
    Program program;

    @PostMapping("/get")
    public ResponseEntity<Float> getCharges(@RequestBody ChargesComputeRequest charges) {
        //we need to fetch charges file/json using programId

        AllPolicies allPolicies = program.getProgramPolicy(charges.getProgramId());
        ChargePolicy chargePolicy = allPolicies.getChargePolicy();
        BNPLPolicy bnplPolicy = allPolicies.getBnplPolicy();
        double value = 0;
        BillingDto tempBill = bill.getBill(charges.getBillId());
        switch (charges.getEventType()) {
            //todo :: fix start date here..
            case BILL_DATE_TO_DUE_DATE:
                value = computeCharges(chargePolicy.getChargeRules(), charges.getEventType(), new Date(Date.parse(bnplPolicy.getBillDate())), tempBill.getBillAmount());
                break;
            case DUE_DATE_TO_HARD_BLOCK:
                value = computeCharges(chargePolicy.getChargeRules(), charges.getEventType(), new Date(Date.parse(bnplPolicy.getDueDate())), tempBill.getBillAmount());
                break;
            case HARD_BLOCK_TO_PERMANENT_BLOCK:
                value = computeCharges(chargePolicy.getChargeRules(), charges.getEventType(), new Date(Date.parse(bnplPolicy.getHardDueDate())), tempBill.getBillAmount());
                break;
            default:
                value = 10;
        }
        return new ResponseEntity(value, HttpStatus.OK);
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
}
