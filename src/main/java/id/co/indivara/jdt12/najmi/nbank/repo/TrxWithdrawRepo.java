package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;


public interface TrxWithdrawRepo extends JpaRepository<TrxWithdraw, Long> {
    List<TrxWithdraw> findAllByAccount(Account ac);

    @Query(value = "SELECT SUM(t.amount) FROM TrxWithdraw t WHERE EXTRACT(epoch FROM (DATE_TRUNC('day', CURRENT_TIMESTAMP) - DATE_TRUNC('day', t.timestamp))) < 1 AND t.account = ?1")
    BigDecimal getTodayTransaction(Account account);
}
