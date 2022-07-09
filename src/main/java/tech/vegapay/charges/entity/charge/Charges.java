package tech.vegapay.charges.entity.charge;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="charges")
@Data
public class Charges {

    @Id
    private String id;

    @Column(name="charge_config", nullable=false, updatable=false)
    public String chargeConfig;
}
