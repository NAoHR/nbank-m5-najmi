package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
public interface TrxDepositRepo extends JpaRepository<TrxDeposit, Long> {
    List<TrxDeposit> findAllByAccount(Account ac);

    @Query(value = "SELECT SUM(t.amount) FROM TrxDeposit t WHERE EXTRACT(epoch FROM (DATE_TRUNC('day', CURRENT_TIMESTAMP) - DATE_TRUNC('day', t.timestamp))) < 1 AND t.account = ?1")
    BigDecimal getTodayTransaction(Account account);
}
