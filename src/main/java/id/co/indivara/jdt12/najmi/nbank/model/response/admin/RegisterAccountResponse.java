package id.co.indivara.jdt12.najmi.nbank.model.response.admin;

import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterAccountResponse {
    private UUID accountId;

    private String accountNumber;

    private StatusEnum status;

    private AccountTypeEnum accountType;

    private BigDecimal balance;

    private Timestamp openDate;

    private Integer depositMonth;
}
