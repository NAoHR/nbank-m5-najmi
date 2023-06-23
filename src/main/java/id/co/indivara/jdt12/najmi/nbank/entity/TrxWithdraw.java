package id.co.indivara.jdt12.najmi.nbank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Trx_Withdraws")
public class TrxWithdraw {
    @Id
    @Column(name = "withdraw_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "withdraw_sequence")
    private Long withdrawId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number")
    @JsonIgnore
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    private Timestamp timestamp;

    @PrePersist
    private void tmstmp(){
        timestamp = Timestamp.valueOf(LocalDateTime.now());
    }
}
