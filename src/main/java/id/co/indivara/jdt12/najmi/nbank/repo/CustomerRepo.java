package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface CustomerRepo extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByEmailOrIdentityNumber(String email, String identityNumber);
}
