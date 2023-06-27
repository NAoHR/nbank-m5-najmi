package id.co.indivara.jdt12.najmi.nbank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedeemWithdrawOrDepositRequest {
    @NotNull
    private UUID uuid;

    private BigDecimal money;
}
