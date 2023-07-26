package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxCardless;
import id.co.indivara.jdt12.najmi.nbank.enums.CardLessEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrxCardlessRepo extends JpaRepository<TrxCardless, UUID> {

    @Query(value = "SELECT t from TrxCardless t WHERE t.account=?1 AND t.redeemed=?2 AND t.type=?3")
    List<TrxCardless> getTrxByOption(Account account, Boolean status, CardLessEnum type);

    @Query(value = "SELECT t from TrxCardless t WHERE t.account=?1 AND type=?2")
    List<TrxCardless> getTrxByOption(Account account,  CardLessEnum type);

}
