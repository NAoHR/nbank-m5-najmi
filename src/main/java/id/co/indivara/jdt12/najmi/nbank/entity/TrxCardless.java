package id.co.indivara.jdt12.najmi.nbank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import id.co.indivara.jdt12.najmi.nbank.enums.CardLessEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "Trx_Cardless")
public class TrxCardless {
    @Id
    @Column(name = "cardless_id", nullable = false)
    private UUID cardlessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number", nullable = false)
    @JsonIgnore
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardLessEnum type;
    @Column(nullable = false)
    private boolean reedemed;

    @Column(name = "created_time", nullable = false)
    private Timestamp createdTime;

    @Column(name = "redeemed_time")
    private Timestamp redeemedTime;
}
