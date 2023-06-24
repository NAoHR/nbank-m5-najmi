package id.co.indivara.jdt12.najmi.nbank.model.request.admin;

import id.co.indivara.jdt12.najmi.nbank.enums.HighestEducationEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.MaritalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterCustomerReq {

    @NotNull
    @Size(min = 16, max = 16)
    private String identityNumber;

    @NotNull
    @Size(min=2, max = 50)
    private String firstName;

    private String lastName;

    @NotNull
    @Pattern(regexp = "^[0-9]+$", message = "number only")
    private String phoneNumber;

    @NotNull
    @Email
    private String email;

    @NotNull
    private LocalDate dob;

    @NotNull
    @Size(min = 15)
    private String address;

    @NotNull
    @Size(min=4)
    private String occupation;

    @NotNull
    private BigDecimal income;

    @NotNull
    private MaritalStatusEnum maritalStatus;

    @NotNull
    private HighestEducationEnum highestEducation;

    @NotNull
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must be minimum eight characters, at least one letter and one number")
    private String password;

}
