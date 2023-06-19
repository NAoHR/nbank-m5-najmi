package id.co.indivara.jdt12.najmi.nbank;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.HighestEducationEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.MaritalStatusEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterCustomerReq;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerRepo;
import id.co.indivara.jdt12.najmi.nbank.security.BCrypt;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountCustomerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TestHelper {
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    AccountCustomerHelper accountCustomerHelper;

    public final Customer createOkCustomer(){
        RegisterCustomerReq customerReq = RegisterCustomerReq.builder()
                .identityNumber("1234567890123456")
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

        customerRepo.save(customer);

        return customer;

    }

    public final Account createOkAccount(AccountTypeEnum accountType, Integer month){ // default
        BigDecimal balance = BigDecimal.valueOf(accountType.equals(AccountTypeEnum.SAVINGS) ? 500_000 : accountType.equals(AccountTypeEnum.TIME_DEPOSIT) ? 8_000_000 : 0);

        Account account = Account.builder()
                .accountId(UUID.randomUUID())
                .customer(createOkCustomer())
                .accountNumber(accountCustomerHelper.generateAccountNumber())
                .password(BCrypt.hashpw("password123", BCrypt.gensalt()))
                .status(StatusEnum.ACTIVE)
                .accountType(accountType)
                .balance(balance)
                .depositMonth(month)
                .openDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        accountRepo.save(account);
        return account;
    }
    public final Account createOkAccount(AccountTypeEnum accountType, Integer month, BigDecimal money){ // money

        Account account = Account.builder()
                .accountId(UUID.randomUUID())
                .customer(createOkCustomer())
                .accountNumber(accountCustomerHelper.generateAccountNumber())
                .password(BCrypt.hashpw("password123", BCrypt.gensalt()))
                .status(StatusEnum.ACTIVE)
                .accountType(accountType)
                .balance(money)
                .depositMonth(month)
                .openDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        accountRepo.save(account);
        return account;
    }
}
