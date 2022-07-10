package tech.vegapay.charges.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.vegapay.charges.entity.ChargesComputeRequest;
import tech.vegapay.commons.dto.charges.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@CrossOrigin()
@RestController()
@RequestMapping("/chargesProcessing")
public class ChargesController {

    @PostMapping("/get")
    public ResponseEntity<Charges> getCharges(@RequestBody ChargesComputeRequest charges) {
        //we need to fetch charges file/json using programId
        ChargesDTO chargesDTO = new ChargesDTO();
        double value = 0;
        double billAmount = 0;
        switch (charges.getEventType()) {
            case BILL_DATE_TO_DUE_DATE:
                value = computeCharges(chargesDTO.getChargeConfig(), charges.getEventType(), new Date(), billAmount);
                break;
            case DUE_DATE_TO_HARD_BLOCK:
                value = computeCharges(chargesDTO.getChargeConfig(), charges.getEventType(), new Date(), billAmount);
                break;
            case HARD_BLOCK_TO_PERMANENT_BLOCK:
                value = computeCharges(chargesDTO.getChargeConfig(), charges.getEventType(), new Date(), billAmount);
                break;
            default:
                value = 10;
        }
        return new ResponseEntity(value, HttpStatus.OK);
    }

    private double computeCharges(ChargeConfig[] chargeConfigs, Category category, Date startDate, double billAmount) {
        for (ChargeConfig temp : chargeConfigs) {
            if (temp.getCategory().equals(category)) {
                PerDayCharge perDayCharge = temp.getPerDayCharge();
                if (perDayCharge != null) {
                    return getEstimatedCharges(startDate, billAmount, perDayCharge);
                } else {
                    SlabCharges[] slabCharges = temp.getSlabCharges();
                    for (SlabCharges tempSlabCharge : slabCharges) {
                        if (Double.compare(tempSlabCharge.getStart(), billAmount) == -1 && Double.compare(billAmount, tempSlabCharge.getEnd()) == -1) {
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
        if (charges.getChargesType().equals(ChargesType.RUPEES)) {
            double tempCharge = charges.getValue() * days;
            if (!charges.isInclusiveOfGST()) {
                tempCharge += tempCharge * charges.getGstTaxPercentage() / 100;
            }
            return tempCharge;
        } else if (charges.getChargesType().equals(ChargesType.PERCENTAGE)) {
            double tempCharge = billAmount * charges.getValue() * days / (365 * 100);
            if (!charges.isInclusiveOfGST()) {
                tempCharge += tempCharge * charges.getGstTaxPercentage() / 100;
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
