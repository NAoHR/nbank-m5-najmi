package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerAuthRepo extends JpaRepository<CustomerAuth, Long> {
}
