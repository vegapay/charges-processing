package tech.vegapay.charges.entity;

import lombok.Data;
import tech.vegapay.charges.entity.charge.ChargesType;

@Data
public class SlabCharges extends AbstractCharges {
    double start;
    double end;
}
