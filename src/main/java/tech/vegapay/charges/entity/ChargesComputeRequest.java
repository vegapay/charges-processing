package tech.vegapay.charges.entity;

import lombok.Data;
import tech.vegapay.commons.dto.policies.charges.ChargeCategory;

@Data
public class ChargesComputeRequest {
    ChargeCategory eventType;
    String programId;
    String customerId;
    String transactionId;
    String billId;
}