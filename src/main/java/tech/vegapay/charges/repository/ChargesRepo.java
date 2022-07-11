package tech.vegapay.charges.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vegapay.commons.dto.policies.charges.ChargePolicy;

@Repository
public interface ChargesRepo extends JpaRepository<ChargePolicy, String> {

}
