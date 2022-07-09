package tech.vegapay.charges.entity;

import lombok.Data;
import tech.vegapay.charges.entity.charge.ChargesType;

@Data
public  abstract class AbstractCharges {
    ChargesType chargesType;
    int value;
    boolean inclusiveOfGST;
    double gstTaxPercentage;
}
