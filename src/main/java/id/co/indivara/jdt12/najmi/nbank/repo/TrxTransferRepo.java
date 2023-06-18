package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TrxTransferRepo extends JpaRepository<TrxTransfer, Long> {
    List<TrxTransfer> findAllByAccount(Account ac);
}
