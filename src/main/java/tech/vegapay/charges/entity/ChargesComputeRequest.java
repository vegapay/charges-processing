package tech.vegapay.charges.entity;

import lombok.Data;
import tech.vegapay.commons.dto.charges.Category;

@Data
public class ChargesComputeRequest {
    Category eventType;
    String programId;
    String customerId;
    String transactionId;
    String billId;
}