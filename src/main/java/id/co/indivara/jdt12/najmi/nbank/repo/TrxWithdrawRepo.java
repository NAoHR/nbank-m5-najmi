package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TrxWithdrawRepo extends JpaRepository<TrxWithdraw, Long> {
    List<TrxWithdraw> findAllByAccount(Account ac);
}
