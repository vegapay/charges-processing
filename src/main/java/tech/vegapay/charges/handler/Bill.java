package tech.vegapay.charges.handler;

import tech.vegapay.commons.dto.BillDto;

import java.util.List;

public interface Bill {
    BillDto getBill(String billId);

    List<BillDto> getBillByAccountId(String accountId);

    BillDto getLatestBill(String accountId);
}
