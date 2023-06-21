package id.co.indivara.jdt12.najmi.nbank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import id.co.indivara.jdt12.najmi.nbank.enums.HighestEducationEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.MaritalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Mst_Customers")
public class Customer {
    @Id
    @Column(name="customer_id", nullable = false)
    private UUID customerId; // auto generate

    @Column(name = "identity_number", nullable = false, length = 16, unique = true)
    @Pattern(regexp = "^[0-9]+$")
    private String identityNumber;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;


    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    private LocalDate dob;

    @NotNull
    private String address;

    @NotNull
    @Size(min = 4)
    private String occupation;

    @Column(nullable = false)
    private BigDecimal income;

    @Column(name = "marital_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaritalStatusEnum maritalStatus;

    @Column(name = "highest_education", nullable = false)
    @Enumerated(EnumType.STRING)
    private HighestEducationEnum highestEducation;

    @Column(nullable = false)
    @Size(min = 8)
    @JsonIgnore
    private String password;
}
