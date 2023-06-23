package id.co.indivara.jdt12.najmi.nbank.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Trx_Transfers")
public class TrxTransfer {
    @Id
    @Column(name = "transfer_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "transfer_sequence")
    private Long transferId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number")
//    @JsonIgnore
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination",referencedColumnName = "account_number")
//    @JsonIgnore
    private Account destination;

    private Timestamp timestamp;

    @PrePersist
    private void tmstmp(){
        timestamp = Timestamp.valueOf(LocalDateTime.now());
    }
}
