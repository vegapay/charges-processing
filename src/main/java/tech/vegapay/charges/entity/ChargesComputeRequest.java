package tech.vegapay.charges.entity;

import lombok.Data;
import lombok.Getter;
import tech.vegapay.charges.entity.charge.Category;

@Data
public class ChargesComputeRequest {
    Category eventType;
    String programId;
    String customerId;
    String transactionId;
    String billId;
}