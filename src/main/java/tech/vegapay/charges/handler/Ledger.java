package tech.vegapay.charges.handler;

import tech.vegapay.commons.dto.LedgerEntryDto;

public interface Ledger {

    LedgerEntryDto createLedgerEntry(LedgerEntryDto ledger);
}
