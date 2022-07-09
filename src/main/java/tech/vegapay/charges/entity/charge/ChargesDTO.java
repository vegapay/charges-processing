package tech.vegapay.charges.entity.charge;

import com.google.gson.Gson;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.vegapay.charges.entity.ChargeConfig;

import java.util.UUID;

@Data
public class ChargesDTO {

    public ChargeConfig[] chargeConfig;
}
