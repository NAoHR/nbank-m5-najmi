package id.co.indivara.jdt12.najmi.nbank.model.request.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthAccountRequest {
    @NotNull
    private UUID acid;
    @NotNull
    private String pin;
}
