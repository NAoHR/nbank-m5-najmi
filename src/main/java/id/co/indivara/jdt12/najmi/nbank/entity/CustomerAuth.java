package id.co.indivara.jdt12.najmi.nbank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Customer_Auths")
public class CustomerAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_sequence")
    private Long authId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="customer_id")
    @JsonIgnore
    private Customer customer;

    private String token;
}