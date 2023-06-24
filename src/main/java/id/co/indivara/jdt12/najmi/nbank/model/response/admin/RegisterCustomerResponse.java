package id.co.indivara.jdt12.najmi.nbank.model.response.admin;

import id.co.indivara.jdt12.najmi.nbank.enums.HighestEducationEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.MaritalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterCustomerResponse {
    private UUID customerId;
    private String identityNumber;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private LocalDate dob;
    private String address;
    private String occupation;
    private BigDecimal income;
    private MaritalStatusEnum maritalStatus;
    private HighestEducationEnum highestEducation;
}
