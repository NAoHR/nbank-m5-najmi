package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TrxDepositRepo extends JpaRepository<TrxDeposit, Long> {
    List<TrxDeposit> findAllByAccount(Account ac);
}
