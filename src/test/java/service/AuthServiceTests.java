package service;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.AccountAuth;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import id.co.indivara.jdt12.najmi.nbank.model.request.account.AuthAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.customer.AuthCustomerRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.TokenResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerRepo;
import id.co.indivara.jdt12.najmi.nbank.security.JsonWebToken;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AuthHelper;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.AuthServiceImpl;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.ValidatorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.lang.model.type.NullType;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class AuthServiceTests {
    @Mock
    private ValidatorService validatorService;
    @Mock
    private CustomerRepo customerRepo;
    @Mock
    private CustomerAuthRepo customerAuthRepo;
    @Mock
    private AccountRepo accountRepo;
    @Mock
    private AccountAuthRepo accountAuthRepo;
    @Mock
    private JsonWebToken jsonWebToken;
    @Mock
    private AuthHelper authHelper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void customerLoginTest(){
        String token = "waduh ini token";

        Customer customer = new Customer();
        customer.setPassword("123456");
        AuthCustomerRequest request = AuthCustomerRequest
                .builder()
                .email("waduh@gmail.com")
                .password("123456")
                .build();

        CustomerAuth customerAuth = CustomerAuth
                .builder()
                .customer(customer)
                .authId(123456L)
                .token("waduh token lama")
                .build();

        doNothing().when(validatorService).validate(anyString());
        when(customerRepo.findByEmail(anyString())).thenReturn(Optional.of(customer));
        doNothing().when(authHelper).validatePassword(anyString(), anyString());
        when(customerAuthRepo.findByCustomer(any(Customer.class))).thenReturn(customerAuth);
        when(jsonWebToken.generateToken(any(Customer.class))).thenReturn(token);
        when(customerAuthRepo.save(any(CustomerAuth.class))).thenReturn(customerAuth);

        TokenResponse response = authService.customerLogin(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getToken(), token);

        verify(validatorService).validate(any());
        verify(customerRepo).findByEmail(anyString());
        verify(authHelper).validatePassword(anyString(), anyString());
        verify(customerAuthRepo).findByCustomer(any());
        verify(jsonWebToken).generateToken(any(Customer.class));
        verify(customerAuthRepo).save(any());

        verifyNoMoreInteractions(validatorService, customerRepo, authHelper, customerRepo, jsonWebToken);
    }

    @Test
    public void customerLogoutTest(){
        CustomerAuth customerAuth = CustomerAuth
                .builder()
                .customer(new Customer())
                .authId(123456L)
                .token("waduh token lama")
                .build();

        when(customerAuthRepo.findByCustomer(any())).thenReturn(customerAuth);
        when(customerAuthRepo.save(any())).thenReturn(customerAuth);

        NullType response = authService.customerLogout(new Customer());
        Assertions.assertNull(response);

        verify(customerAuthRepo).findByCustomer(any());
        verify(customerAuthRepo).save(any());

        verifyNoMoreInteractions(customerAuthRepo);
    }

    @Test
    public void accountLoginTest(){
        String token = "waduh ini token";

        Account account = new Account();
        account.setPassword("123456");
        AuthAccountRequest request = AuthAccountRequest
                .builder()
                .acid(UUID.randomUUID())
                .pin("123456")
                .build();

        AccountAuth accountAuth = AccountAuth
                .builder()
                .account(account)
                .authId(123456L)
                .token("waduh token lama")
                .build();

        doNothing().when(validatorService).validate(any());
        when(accountRepo.findById(any())).thenReturn(Optional.of(account));
        doNothing().when(authHelper).validatePassword(anyString(), anyString());
        when(jsonWebToken.generateToken(any(Account.class))).thenReturn(token);
        when(accountAuthRepo.findByAccount(any())).thenReturn(accountAuth);
        when(accountAuthRepo.save(any())).thenReturn(accountAuth);

        TokenResponse response = authService.accountLogin(request);
        Assertions.assertNotNull(response);

        verify(validatorService).validate(any());
        verify(accountRepo).findById(any());
        verify(authHelper).validatePassword(anyString(), anyString());
        verify(accountAuthRepo).findByAccount(any());
        verify(jsonWebToken).generateToken(any(Account.class));
        verify(accountAuthRepo).save(any());

        verifyNoMoreInteractions(validatorService, accountRepo, authHelper, accountAuthRepo, jsonWebToken);
    }

    @Test
    public void accountLogoutTest(){
        AccountAuth accountAuth = AccountAuth
                .builder()
                .account(new Account())
                .authId(123456L)
                .token("waduh token lama")
                .build();

        when(accountAuthRepo.findByAccount(any())).thenReturn(accountAuth);
        when(accountAuthRepo.save(any())).thenReturn(accountAuth);

        NullType response = authService.accountLogout(new Account());
        Assertions.assertNull(response);

        verify(accountAuthRepo).findByAccount(any());
        verify(accountAuthRepo).save(any());

        verifyNoMoreInteractions(accountAuthRepo);
    }


}
