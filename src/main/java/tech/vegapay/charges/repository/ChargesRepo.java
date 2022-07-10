package tech.vegapay.charges.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vegapay.commons.dto.charges.Charges;

@Repository
public interface ChargesRepo extends JpaRepository<Charges, String> {

}
