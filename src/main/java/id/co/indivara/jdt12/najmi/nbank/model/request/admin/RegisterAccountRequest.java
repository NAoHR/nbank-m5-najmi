package id.co.indivara.jdt12.najmi.nbank.model.request.admin;

import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterAccountRequest {
    @NotNull
    private String accountPassword;
    @NotNull
    private StatusEnum status;
    @NotNull
    private AccountTypeEnum accountType;

    @Min(0)
    private BigDecimal balance;

    @NotNull
    private UUID customerId;

    @Min(0)
    private Integer depositMonth;
}
