package tech.vegapay.charges.entity;

import lombok.Data;
import tech.vegapay.charges.entity.charge.Category;
import tech.vegapay.charges.entity.charge.Format;

@Data
public class ChargeConfig {
    Category category;
    Format format;
    SlabCharges[] slabCharges;
    PerDayCharge perDayCharge;

}
