package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface CustomerRepo extends JpaRepository<Customer, UUID> {
    Customer findByEmail(String email);

    boolean existsByEmail(String email);
}
