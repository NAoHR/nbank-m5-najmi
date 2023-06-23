package id.co.indivara.jdt12.najmi.nbank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.AccountAuth;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.account.AuthAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.customer.AuthCustomerRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.TokenResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import id.co.indivara.jdt12.najmi.nbank.service.AdminService;
import id.co.indivara.jdt12.najmi.nbank.service.AuthService;
import jdk.nashorn.internal.parser.Token;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.lang.model.type.NullType;
import javax.validation.constraints.Null;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AppControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerAuthRepo customerAuthRepo;

    @Autowired
    private AccountAuthRepo accountAuthRepo;

    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private AdminService adminService;

    @Autowired
    private TrxTransferRepo trxTransferRepo;
    @Autowired
    private TrxWithdrawRepo trxWithdrawRepo;

    @Autowired
    private TrxDepositRepo trxDepositRepo;

    @Autowired
    private AuthService authService;

    /*
     * 1. Display Detail Customer - done
     *   1.1 display detail customer failed token expired - done
     * 2. Show Customer Accounts - done
     *   2.1 show customer accounts error -done
     * 3. show account detail and transaction - done
     *   3.1 show account detail and transaction failed token expired - done
     * 2. show account detail and trasaction - done
     *   2.1 show account detail and trasaction failed token expired - done
     * 3. transfer via app - done
     * 4. transfer via app failed token expired - done
     */

    @Before // jalanin setiap sebelum test
    public void setup(){
        objectMapper.registerModule(new JavaTimeModule());
        customerAuthRepo.deleteAll();
        accountAuthRepo.deleteAll();
        trxDepositRepo.deleteAll();
        trxWithdrawRepo.deleteAll();
        trxTransferRepo.deleteAll();
        accountRepo.deleteAll();
        customerRepo.deleteAll();
    }

    private void accountExpired(MvcResult result) throws Exception{
        WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
        log.info(response.toString());
        Assertions.assertNull(response.getData());
        Assertions.assertNotNull(response.getError());
        Assertions.assertEquals("Session Expired", response.getError().getName());
        Assertions.assertEquals("Please Login Again", response.getError().getDetail());
    }

    @Test
    public void displayDetailCustomer() throws Exception{
        Customer customer = testHelper.createOkCustomer();
        AuthCustomerRequest authCustomerRequest = AuthCustomerRequest.builder()
                .email(customer.getEmail())
                .password("Password123")
                .build();
        String token = authService.customerLogin(authCustomerRequest).getToken();

        mockMvc.perform(
                get("/api/app/customer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<Customer, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<Customer, NullType>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());
        });
    }

    @Test
    public void displayDetailCustomerFailedTokenExpired() throws Exception{
        Customer customer = testHelper.createOkCustomer();
        String token = testHelper.customJwtExpiredGenerator(customer.getCustomerId(), 0);
        CustomerAuth cauth = customerAuthRepo.findByCustomer(customer);
        cauth.setToken(token);
        customerAuthRepo.save(cauth);

        mockMvc.perform(
                get("/api/app/customer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(this::accountExpired);
    }

    @Test
    public void displayDetailCustomerAccount() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        AuthCustomerRequest authCustomerRequest = AuthCustomerRequest.builder()
                .email(account.getCustomer().getEmail())
                .password("Password123")
                .build();
        String token = authService.customerLogin(authCustomerRequest).getToken();

        mockMvc.perform(
                get("/api/app/customer/accounts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<Account>, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<List<Account>, NullType>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());
        });
    }

    @Test
    public void displayDetailCustomerAccountFailedTokenExpired() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        String token = testHelper.customJwtExpiredGenerator(account.getAccountId(), 0);

        AccountAuth aauth = accountAuthRepo.findByAccount(account);
        aauth.setToken(token);
        accountAuthRepo.save(aauth);

        mockMvc.perform(
                get("/api/app/customer/accounts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(this::accountExpired);
    }

    @Test
    public void showAccountDetailAndTransaction() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        AuthAccountRequest authAccountRequest = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();
        String token = authService.accountLogin(authAccountRequest).getToken();

        mockMvc.perform(
                get("/api/app/customer/account/detail")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<HashMap<String ,Object>, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<HashMap<String ,Object>, NullType>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());
        });
    }

    @Test
    public void showAccountDetailAndTransactionFailed() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);


        String token = testHelper.customJwtExpiredGenerator(account.getAccountId(), 0);

        AccountAuth accountAuth = accountAuthRepo.findByAccount(account);
        accountAuth.setToken(token);
        accountAuthRepo.save(accountAuth);

        mockMvc.perform(
                get("/api/app/customer/account/detail")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(this::accountExpired);
    }

    @Test
    public void transferWithApp() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        Account destination = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();
        TokenResponse t = authService.accountLogin(req);

        AtmAndAppTransferRequest request = AtmAndAppTransferRequest.builder()
                .amount(BigDecimal.valueOf(1_000))
                .destination(destination.getAccountNumber())
                .build();

        mockMvc.perform(
                post("/api/app/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + t.getToken())
        ).andExpect(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<TrxTransferReferencedId,NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TrxTransferReferencedId,NullType>>() {});
            log.info(response.toString());
            Account acResp = accountRepo.findById(account.getAccountId()).orElseThrow(null);
            Account desResp = accountRepo.findById(destination.getAccountId()).orElseThrow(null);

            Assertions.assertEquals(0, acResp.getBalance().compareTo(account.getBalance().subtract(BigDecimal.valueOf(1_000))));
            Assertions.assertEquals(0, desResp.getBalance().compareTo(account.getBalance().add(BigDecimal.valueOf(1_000))));
            Assertions.assertNotNull(response.getData());
            Assertions.assertNull(response.getError());
        });
    }

    @Test
    public void transferFailedSessionExpiredTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        Account destination = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        String t = testHelper.customJwtExpiredGenerator(account.getAccountId(), 0);
        AccountAuth accountAuth = accountAuthRepo.findByAccount(account);
        accountAuth.setToken(t);
        accountAuthRepo.save(accountAuth);

        AtmAndAppTransferRequest request = AtmAndAppTransferRequest.builder()
                .amount(BigDecimal.valueOf(50_000))
                .destination(destination.getAccountNumber())
                .build();

        mockMvc.perform(
                post("/api/atm/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + t)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(this::accountExpired);
    }

}
