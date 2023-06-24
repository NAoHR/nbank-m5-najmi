package id.co.indivara.jdt12.najmi.nbank.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawRequest {
    @NotNull
    private String accountNumber;
    @NotNull
    private BigDecimal money;
}
