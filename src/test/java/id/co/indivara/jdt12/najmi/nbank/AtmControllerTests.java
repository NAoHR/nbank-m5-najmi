package id.co.indivara.jdt12.najmi.nbank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.AccountAuth;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.account.AuthAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.TokenResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import id.co.indivara.jdt12.najmi.nbank.service.AdminService;
import id.co.indivara.jdt12.najmi.nbank.service.AuthService;
import io.jsonwebtoken.Jwt;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AtmControllerTests {

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


    /*
     * 1. display transaction activity - done
     *   1.1 failed token expired - done
     * 2. deposit via atm - done
     *    2.1 failed token expired - done
     * 3. withdraw via atm - done
     *    3.1 failed token expired - done
     * 4. transfer via atm - done
     *    4.1 failed token expired - done
     *    4.2 failed because it must be equals or more than 5k - done
     */

    private void accountExpired(MvcResult result) throws Exception{
        WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
        log.info(response.toString());
        Assertions.assertNull(response.getData());
        Assertions.assertNotNull(response.getError());
        Assertions.assertEquals("Session Expired", response.getError().getName());
        Assertions.assertEquals("Please Login Again", response.getError().getDetail());
    }

    @Test
    public void displayTransactionActivityTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();
        TokenResponse t = authService.accountLogin(req);

        mockMvc.perform(
                get("/api/atm/transaction/activity")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + t.getToken())
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            WebResponse<HashMap<String, Object>,NullType > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<HashMap<String, Object>,NullType >>() {});
            log.info(response.toString());
            Assertions.assertNotNull(response.getData());
            Assertions.assertNull(response.getError());
        });
    }

    @Test
    public void displayTransactionActivityFailedSessionExpiredTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        String t = testHelper.customJwtExpiredGenerator(account.getAccountId(), 0);
        AccountAuth accountAuth = accountAuthRepo.findByAccount(account);
        accountAuth.setToken(t);
        accountAuthRepo.save(accountAuth);

        mockMvc.perform(
                get("/api/atm/transaction/activity")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + t)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(this::accountExpired);
    }

    @Test
    public void depositViaAtm() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();
        TokenResponse t = authService.accountLogin(req);

        AtmDepositWithdrawRequest request = AtmDepositWithdrawRequest.builder()
                .money(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/atm/transaction/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + t.getToken())
        ).andExpect(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<TrxDeposit,NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TrxDeposit,NullType>>() {});
            log.info(response.toString());
            Account acResp = accountRepo.findById(account.getAccountId()).orElseThrow(null);
            Assertions.assertEquals(0, acResp.getBalance().compareTo(account.getBalance().add(BigDecimal.valueOf(50_000))));
            Assertions.assertNotNull(response.getData());
            Assertions.assertNull(response.getError());
        });
    }

    @Test
    public void depositFailedSessionExpiredTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        String t = testHelper.customJwtExpiredGenerator(account.getAccountId(), 0);
        AccountAuth accountAuth = accountAuthRepo.findByAccount(account);
        accountAuth.setToken(t);
        accountAuthRepo.save(accountAuth);
        AtmDepositWithdrawRequest request = AtmDepositWithdrawRequest.builder()
                .money(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/atm/transaction/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + t)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(this::accountExpired);
    }


    @Test
    public void witdrawViaAtm() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();
        TokenResponse t = authService.accountLogin(req);

        AtmDepositWithdrawRequest request = AtmDepositWithdrawRequest.builder()
                .money(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/atm/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + t.getToken())
        ).andExpect(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<TrxDeposit,NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TrxDeposit,NullType>>() {});
            log.info(response.toString());
            Account acResp = accountRepo.findById(account.getAccountId()).orElseThrow(null);
            Assertions.assertEquals(0, acResp.getBalance().compareTo(account.getBalance().subtract(BigDecimal.valueOf(50_000))));
            Assertions.assertNotNull(response.getData());
            Assertions.assertNull(response.getError());
        });
    }

    @Test
    public void withdrawFailedSessionExpiredTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        String t = testHelper.customJwtExpiredGenerator(account.getAccountId(), 0);
        AccountAuth accountAuth = accountAuthRepo.findByAccount(account);
        accountAuth.setToken(t);
        accountAuthRepo.save(accountAuth);
        AtmDepositWithdrawRequest request = AtmDepositWithdrawRequest.builder()
                .money(BigDecimal.valueOf(50_000))
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


    @Test
    public void transferViaAtm() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        Account destination = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();
        TokenResponse t = authService.accountLogin(req);

        AtmAndAppTransferRequest request = AtmAndAppTransferRequest.builder()
                .amount(BigDecimal.valueOf(50_000))
                .destination(destination.getAccountNumber())
                .build();

        mockMvc.perform(
                post("/api/atm/transaction/transfer")
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

            Assertions.assertEquals(0, acResp.getBalance().compareTo(account.getBalance().subtract(BigDecimal.valueOf(50_000))));
            Assertions.assertEquals(0, desResp.getBalance().compareTo(account.getBalance().add(BigDecimal.valueOf(50_000))));
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

    @Test
    public void transferFailedMoneyBelow5k() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        Account destination = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        AuthAccountRequest req = AuthAccountRequest.builder()
                .acid(account.getAccountId())
                .pin("123456")
                .build();
        TokenResponse t = authService.accountLogin(req);

        AtmAndAppTransferRequest request = AtmAndAppTransferRequest.builder()
                .amount(BigDecimal.valueOf(4_000))
                .destination(destination.getAccountNumber())
                .build();

        mockMvc.perform(
                post("/api/atm/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + t.getToken())
        ).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType,Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType,Object>>() {});
            log.info(response.toString());

            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
        });
    }

}
