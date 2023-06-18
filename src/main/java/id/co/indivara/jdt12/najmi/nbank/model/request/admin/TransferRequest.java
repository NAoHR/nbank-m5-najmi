package id.co.indivara.jdt12.najmi.nbank.model.request.admin;

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
public class TransferRequest {
    @NotNull
    private UUID from;
    private UUID destination;
    private BigDecimal amount;
}
