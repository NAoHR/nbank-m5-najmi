package id.co.indivara.jdt12.najmi.nbank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrxTransferReferencedId {
    private Long transferId;
    private String account;
    private BigDecimal amount;
    private String destination;
    private Timestamp timestamp;
}
