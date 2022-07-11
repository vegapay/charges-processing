package tech.vegapay.charges.handler;

import tech.vegapay.commons.dto.BillingDto;

public interface Bill {
    BillingDto getBill(String billId);
}
