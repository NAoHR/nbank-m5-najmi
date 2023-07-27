package service;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.AccountAuth;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.HighestEducationEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.MaritalStatusEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterCustomerReq;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterAccountResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterCustomerResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerRepo;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountCustomerHelper;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.AdminServiceImpl;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.ValidatorService;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RequiredArgsConstructor
public class AdminServiceTests {
    @Mock
    private AccountCustomerHelper accountCustomerHelper;
    @Mock
    private ValidatorService validatorService;
    @Mock
    private CustomerRepo customerRepo;
    @Mock
    private AccountRepo accountRepo;
    @Mock
    private AccountService accountService;
    @Mock
    private CustomerAuthRepo customerAuthRepo;
    @Mock
    private AccountAuthRepo accountAuthRepo;
    @InjectMocks
    private AdminServiceImpl adminService;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void registerCustomerTest(){
        RegisterCustomerReq request = RegisterCustomerReq
                .builder()
                .identityNumber("123456789098765")
                .firstName("waduh")
                .lastName("kumalala")
                .phoneNumber("0812345678")
                .email("waduhKumalala@gmail.com")
                .dob(LocalDate.of(2001, 7, 12))
                .address("jalanin dulu aja yuk")
                .maritalStatus(MaritalStatusEnum.MARRIED)
                .highestEducation(HighestEducationEnum.DOCTORATE)
                .password("123456")
                .build();

        doNothing().when(validatorService).validate(any(RegisterCustomerReq.class));
        when(customerRepo.existsByEmailOrIdentityNumber(anyString(), anyString())).thenReturn(false);
        when(customerRepo.save(any(Customer.class))).thenReturn(new Customer());
        when(customerAuthRepo.save(any(CustomerAuth.class))).thenReturn(new CustomerAuth());

        RegisterCustomerResponse result = adminService.registerCustomer(request);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getIdentityNumber(), request.getIdentityNumber());
        Assertions.assertEquals(result.getFirstName(), request.getFirstName());
        Assertions.assertEquals(result.getLastName(), request.getLastName());
        Assertions.assertEquals(result.getPhoneNumber(), request.getPhoneNumber());
        Assertions.assertEquals(result.getEmail(), request.getEmail());
        Assertions.assertEquals(result.getDob(), request.getDob());
        Assertions.assertEquals(result.getAddress(), request.getAddress());
        Assertions.assertEquals(result.getMaritalStatus(), request.getMaritalStatus());
        Assertions.assertEquals(result.getHighestEducation(), request.getHighestEducation());

        verify(validatorService).validate(any(RegisterCustomerReq.class));
        verify(customerRepo).existsByEmailOrIdentityNumber(anyString(), anyString());
        verify(customerRepo).save(any(Customer.class));
        verify(customerAuthRepo).save(any(CustomerAuth.class));
        verifyNoMoreInteractions(validatorService, customerRepo, customerAuthRepo);
    }

    @Test
    public void registerAccountTest(){
        RegisterAccountRequest request = RegisterAccountRequest
                .builder()
                .accountPassword("123456")
                .status(StatusEnum.ACTIVE)
                .accountType(AccountTypeEnum.SAVINGS)
                .balance(BigDecimal.valueOf(1_000_00L))
                .customerId(UUID.randomUUID())
                .depositMonth(0)
                .build();

        Customer customer = new Customer();
        String accountNumber = "12345678";

        doNothing().when(validatorService).validate(any(RegisterAccountRequest.class));
        when(customerRepo.findById(any())).thenReturn(Optional.of(customer));
        when(accountRepo.save(any(Account.class))).thenReturn(new Account());
        when(accountCustomerHelper.generateAccountNumber()).thenReturn(accountNumber);
        when(accountCustomerHelper.customValidateBalanceAndType(any(BigDecimal.class), any(AccountTypeEnum.class))).thenReturn(request.getBalance());
        when(accountCustomerHelper.customValidateUserMonth(any(AccountTypeEnum.class), anyInt())).thenReturn(0);
        when(accountAuthRepo.save(any(AccountAuth.class))).thenReturn(new AccountAuth());

        RegisterAccountResponse response = adminService.registerAccount(request);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getAccountId());
        Assertions.assertEquals(response.getAccountNumber(), accountNumber);
        Assertions.assertEquals(response.getStatus(), request.getStatus());
        Assertions.assertEquals(response.getAccountType(), request.getAccountType());
        Assertions.assertEquals(response.getBalance(), request.getBalance());
        Assertions.assertEquals(response.getDepositMonth(), request.getDepositMonth());

        verify(validatorService).validate(any());
        verify(customerRepo).findById(any());
        verify(accountRepo).save(any());
        verify(accountCustomerHelper).generateAccountNumber();
        verify(accountCustomerHelper).customValidateBalanceAndType(any(), any());
        verify(accountCustomerHelper).customValidateUserMonth(any(), anyInt());
        verify(accountAuthRepo).save(any());
        verifyNoMoreInteractions(validatorService, customerRepo, accountCustomerHelper, accountAuthRepo);
    }
}
