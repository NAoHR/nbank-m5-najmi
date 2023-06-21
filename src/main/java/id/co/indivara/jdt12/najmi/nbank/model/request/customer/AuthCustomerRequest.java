package id.co.indivara.jdt12.najmi.nbank.model.request.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthCustomerRequest {
    @Email
    private String email;
    @NotNull
    private String password;
}
