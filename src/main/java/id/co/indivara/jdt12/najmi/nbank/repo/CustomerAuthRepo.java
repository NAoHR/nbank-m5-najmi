package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CustomerAuthRepo extends JpaRepository<CustomerAuth, Long> {
    CustomerAuth findByCustomer(Customer c);
    Optional<CustomerAuth> findByToken(String token);

    boolean existsByToken(String token);
}
