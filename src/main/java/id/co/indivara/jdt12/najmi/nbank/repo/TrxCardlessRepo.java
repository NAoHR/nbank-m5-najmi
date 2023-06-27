package id.co.indivara.jdt12.najmi.nbank.repo;

import id.co.indivara.jdt12.najmi.nbank.entity.TrxCardless;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrxCardlessRepo extends JpaRepository<TrxCardless, UUID> {
}
