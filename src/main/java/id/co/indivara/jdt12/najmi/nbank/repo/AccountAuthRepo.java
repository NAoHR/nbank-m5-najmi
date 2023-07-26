package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.AccountAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AccountAuthRepo extends JpaRepository<AccountAuth, Long> {
    AccountAuth findByAccount(Account account);
    boolean existsByToken(String token);
}
