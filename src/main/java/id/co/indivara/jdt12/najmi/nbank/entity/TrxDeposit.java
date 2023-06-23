package id.co.indivara.jdt12.najmi.nbank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Trx_Deposits")
public class TrxDeposit {
    @Id
    @Column(name = "deposit_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "deposit_sequence")
    private Long depositId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number")
    @JsonIgnore
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    private Timestamp timestamp;
}
