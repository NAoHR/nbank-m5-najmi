package id.co.indivara.jdt12.najmi.nbank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.AccountAuth;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.model.request.account.AuthAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterCustomerReq;
import id.co.indivara.jdt12.najmi.nbank.model.request.customer.AuthCustomerRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.TokenResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterCustomerResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import id.co.indivara.jdt12.najmi.nbank.service.AdminService;
import id.co.indivara.jdt12.najmi.nbank.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import org.springframework.test.web.servlet.ResultActions;

import javax.lang.model.type.NullType;
import javax.validation.constraints.Null;

import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Slf4j
public class AuthControllerTests {

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
    private  AdminService adminService;

    @Autowired
    private TrxTransferRepo trxTransferRepo;
    @Autowired
    private TrxWithdrawRepo trxWithdrawRepo;

    @Autowired
    private TrxDepositRepo trxDepositRepo;

    @Autowired
    private TrxCardlessRepo trxCardlessRepo;

    @Autowired
    private AuthService authService;
    /*
     * 1. Customer Login Success - done
     * 2. Customer Login Failed
     *   2.1 email not found - done
     *   2.2 wrong password - done
     * 3. Customer Logout Success - done
     * 4. Customer Logout Failed
     *   4.1 Customer Logout Failed (missing token) - done
     *   4.2 Customer Logout Failed (expired token) - done
     *
     * 5. Account Login Success - done
     * 6. Account Login Failed
     *   6.1 uid not found - done
     *   6.2 wrong password - done
     * 7. Account Logout Success - done
     * 8. Account Logout Failed
     *   5.1 Account Logout Failed (missing token) - done
     *   5.2 Account Logout Failed (expired token) - done
     */



    @Before // jalanin setiap sebelum test
    public void setup(){
        objectMapper.registerModule(new JavaTimeModule());
        customerAuthRepo.deleteAll();
        trxCardlessRepo.deleteAll();
        accountAuthRepo.deleteAll();
        trxDepositRepo.deleteAll();
        trxWithdrawRepo.deleteAll();
        trxTransferRepo.deleteAll();
        accountRepo.deleteAll();
        customerRepo.deleteAll();
    }
    private ResultActions request(String link, Object content) throws Exception{
        return mockMvc.perform(
                post(link)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
        );
    }

    @Test
    public final void customerLoginSuccess() throws Exception{
        Customer customer = testHelper.createOkCustomer();

        AuthCustomerRequest req = AuthCustomerRequest.builder()
                .email(customer.getEmail())
                .password("Password123")
                .build();


        request("/api/auth/customer/login", req).andExpect(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<TokenResponse, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TokenResponse, NullType>>() {});
            log.info(response.toString());
            Assertions.assertEquals("Login Success", response.getMessage());
            Assertions.assertNotNull(response.getData());
            TokenResponse token = response.getData();
            Assertions.assertNotNull(token.getToken());

        });

    }

    @Test
    public final void customerLoginFailedEmailNotFound() throws Exception{
        AuthCustomerRequest req = AuthCustomerRequest.builder()
                .email("")
                .password("waduh99")
                .build();


        request("/api/auth/customer/login", req).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Customer Not Found",response.getError().getName());

        });

    }

    @Test
    public final void customerLoginFailedWrongPassword() throws Exception{
        RegisterCustomerReq customer = testHelper.createcustomCustomerReq();
        adminService.registerCustomer(customer);

        AuthCustomerRequest req = AuthCustomerRequest.builder()
                .email(customer.getEmail())
                .password("waduh99")
                .build();


        request("/api/auth/customer/login", req).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Authentication Failed",response.getError().getName());
            Assertions.assertEquals("Wrong Password",response.getError().getDetail());

        });
    }

    @Test
    public final void customerLogoutSuccess() throws Exception{
        RegisterCustomerReq customer = testHelper.createcustomCustomerReq();
        adminService.registerCustomer(customer);

        AuthCustomerRequest auth = AuthCustomerRequest.builder()
                .email(customer.getEmail())
                .password(customer.getPassword())
                .build();

        TokenResponse t = authService.customerLogin(auth);

        mockMvc.perform(
                delete("/api/auth/customer/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + t.getToken())
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            WebResponse<NullType, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, NullType>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNull(response.getError());
            Assertions.assertEquals("Logged Out", response.getMessage());
        });

    }

    @Test
    public final void customerLogoutFailedMissingToken() throws Exception{
        RegisterCustomerReq customer = testHelper.createcustomCustomerReq();
        adminService.registerCustomer(customer);

        AuthCustomerRequest auth = AuthCustomerRequest.builder()
                .email(customer.getEmail())
                .password(customer.getPassword())
                .build();

        TokenResponse t = authService.customerLogin(auth);

        mockMvc.perform(
                delete("/api/auth/customer/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Unauthorized", response.getError().getName());
            Assertions.assertEquals("Make Sure You Are Logged In First", response.getError().getDetail());
        });

    }

    @Test
    public final void customerLogoutFailedExpired() throws Exception{
        RegisterCustomerReq customer = testHelper.createcustomCustomerReq();
        RegisterCustomerResponse resp = adminService.registerCustomer(customer);

        AuthCustomerRequest auth = AuthCustomerRequest.builder()
                .email(customer.getEmail())
                .password(customer.getPassword())
                .build();

        CustomerAuth cauth = CustomerAuth.builder()
                .token(testHelper.customJwtExpiredGenerator(resp.getCustomerId(), 0))
                .customer(customerRepo.findById(resp.getCustomerId()).orElseThrow(null))
                .build();

        customerAuthRepo.save(cauth);


        mockMvc.perform(
                delete("/api/auth/customer/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + cauth.getToken())
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Session Expired", response.getError().getName());
            Assertions.assertEquals("Please Login Again", response.getError().getDetail());
        });

    }


    @Test
    public final void accountLoginSuccess() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);


        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();


        request("/api/auth/account/login", req).andExpect(
                status().isOk()
        ).andDo(result -> {
            WebResponse<TokenResponse, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TokenResponse, NullType>>() {});
            log.info(response.toString());
            Assertions.assertEquals("Login Success", response.getMessage());
            Assertions.assertNotNull(response.getData());
            TokenResponse token = response.getData();
            Assertions.assertNotNull(token.getToken());

        });

    }

    @Test
    public final void accountLoginFailedAcidNotFound() throws Exception{
        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(UUID.randomUUID())
                .pin("123456")
                .build();


        request("/api/auth/account/login", req).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Account Not Found",response.getError().getName());

        });

    }

    @Test
    public final void accountLoginFailedWrongPassword() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("126")
                .build();


        request("/api/auth/account/login", req).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Authentication Failed",response.getError().getName());
            Assertions.assertEquals("Wrong Password",response.getError().getDetail());

        });

    }

    @Test
    public final void accountLogoutSuccess() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();


        TokenResponse t = authService.accountLogin(req);

        mockMvc.perform(
                delete("/api/auth/account/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + t.getToken())
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            WebResponse<NullType, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, NullType>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNull(response.getError());
            Assertions.assertEquals("Logged Out", response.getMessage());
        });

    }

    @Test
    public final void accountLogoutFailedMissingToken() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();

        TokenResponse t = authService.accountLogin(req);

        mockMvc.perform(
                delete("/api/auth/account/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Unauthorized", response.getError().getName());
            Assertions.assertEquals("Make Sure You Are Logged In First", response.getError().getDetail());
        });

    }

    @Test
    public final void accountLogoutFailedExpired() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();

        AccountAuth aauth = AccountAuth.builder()
                .account(account)
                .token(testHelper.customJwtExpiredGenerator(account.getAccountId(), 0))
                .build();

        accountAuthRepo.save(aauth);



        mockMvc.perform(
                delete("/api/auth/customer/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + aauth.getToken())
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Session Expired", response.getError().getName());
            Assertions.assertEquals("Please Login Again", response.getError().getDetail());
        });

    }

}
