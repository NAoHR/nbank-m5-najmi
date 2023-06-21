package id.co.indivara.jdt12.najmi.nbank;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.AccountAuth;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.HighestEducationEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.MaritalStatusEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterCustomerReq;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerRepo;
import id.co.indivara.jdt12.najmi.nbank.security.BCrypt;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountCustomerHelper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Component
public class TestHelper {
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AccountCustomerHelper accountCustomerHelper;

    @Autowired
    private AccountAuthRepo accountAuthRepo;

    @Autowired
    private CustomerAuthRepo customerAuthRepo;



    public RegisterCustomerReq createcustomCustomerReq(){
        return RegisterCustomerReq.builder()
                .identityNumber(generateRandomIdentityNumber())
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("123456780")
                .email("john.doe" + UUID.randomUUID() + "e@ample.com")
                .dob(LocalDate.parse("2004-07-20"))
                .address("123 Main Street")
                .occupation("Engineer")
                .income(new BigDecimal("50000.0"))
                .maritalStatus(MaritalStatusEnum.SINGLE)
                .highestEducation(HighestEducationEnum.BACHELORS_DEGREE)
                .password("Password123")
                .build();
    }

    public final Customer createOkCustomer(){
        RegisterCustomerReq customerReq = createcustomCustomerReq();

        Customer customer = Customer.builder()
                .customerId(UUID.randomUUID())
                .identityNumber(customerReq.getIdentityNumber())
                .firstName(customerReq.getFirstName())
                .lastName(customerReq.getLastName())
                .phoneNumber(customerReq.getPhoneNumber())
                .email(customerReq.getEmail())
                .dob(customerReq.getDob())
                .address(customerReq.getAddress())
                .occupation(customerReq.getOccupation())
                .income(customerReq.getIncome())
                .maritalStatus(customerReq.getMaritalStatus())
                .highestEducation(customerReq.getHighestEducation())
                .password(BCrypt.hashpw(customerReq.getPassword(), BCrypt.gensalt()))
                .build();

        customerAuthRepo.save(
                CustomerAuth.builder()
                        .customer(customerRepo.save(customer))
                        .token(null)
                        .build()
        );

        return customer;

    }

    public final Account createOkAccount(AccountTypeEnum accountType, Integer month){ // default
        BigDecimal balance = BigDecimal.valueOf(accountType.equals(AccountTypeEnum.SAVINGS) ? 500_000 : accountType.equals(AccountTypeEnum.TIME_DEPOSIT) ? 8_000_000 : 0);

        Account account = Account.builder()
                .accountId(UUID.randomUUID())
                .customer(createOkCustomer())
                .accountNumber(accountCustomerHelper.generateAccountNumber())
                .password(BCrypt.hashpw("123456", BCrypt.gensalt()))
                .status(StatusEnum.ACTIVE)
                .accountType(accountType)
                .balance(balance)
                .depositMonth(month)
                .openDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();


        AccountAuth accountAuth = AccountAuth.builder()
                .token(null)
                .account(accountRepo.save(account))
                .build();

        accountAuthRepo.save(accountAuth);
        return account;
    }
    public final Account createOkAccount(AccountTypeEnum accountType, Integer month, BigDecimal money){ // money

        Account account = Account.builder()
                .accountId(UUID.randomUUID())
                .customer(createOkCustomer())
                .accountNumber(accountCustomerHelper.generateAccountNumber())
                .password(BCrypt.hashpw("123456", BCrypt.gensalt()))
                .status(StatusEnum.ACTIVE)
                .accountType(accountType)
                .balance(money)
                .depositMonth(month)
                .openDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        accountRepo.save(account);
        return account;
    }

    public String customJwtExpiredGenerator(UUID id, Integer time){
        return Jwts.builder()
                .setSubject(id.toString())
                .setIssuer("nbank.com")
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .signWith(Keys.hmacShaKeyFor("MJ4IafykM2RAhV2a7c5DUyMwKgrirm4z".getBytes()))
                .compact();
    }

    private String generateRandomIdentityNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(16);

        for (int i = 0; i < 16; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }

        return sb.toString();
    }
}
