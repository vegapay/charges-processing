package tech.vegapay.charges.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.vegapay.commons.dto.policies.charges.ChargeCategory;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargesComputeRequest {
    ChargeCategory eventType;
    String programId;
    String customerId;
    String transactionId;
    String billId;
}