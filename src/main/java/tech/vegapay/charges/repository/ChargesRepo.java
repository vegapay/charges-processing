package tech.vegapay.charges.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vegapay.charges.entity.charge.Charges;

import java.util.UUID;

@Repository
public interface ChargesRepo extends JpaRepository<Charges, String> {

}
