package id.co.indivara.jdt12.najmi.nbank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Account_Auths")
public class AccountAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_sequence")
    private Long authId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    @JsonIgnore
    private Account account;

    private String token;

    @Column(name = "expired_at")
    private Long expiredAt;
}
