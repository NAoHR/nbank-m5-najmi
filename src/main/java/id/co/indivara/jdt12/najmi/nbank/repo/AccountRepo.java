package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface AccountRepo extends JpaRepository<Account, UUID> {
}
