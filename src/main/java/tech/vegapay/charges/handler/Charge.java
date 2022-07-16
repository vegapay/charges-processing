package tech.vegapay.charges.handler;

import tech.vegapay.commons.dto.ChargeDto;

public interface Charge {

    ChargeDto createCharge(ChargeDto chargeDto);
    ChargeDto getChargeById(String chargeId);

}
