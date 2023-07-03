package id.co.indivara.jdt12.najmi.nbank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.co.indivara.jdt12.najmi.nbank.entity.*;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.CardLessEnum;
import id.co.indivara.jdt12.najmi.nbank.model.RedeemWithdrawOrDepositRequest;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.OnlyMoneyDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.account.AuthAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.TokenResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import id.co.indivara.jdt12.najmi.nbank.service.AdminService;
import id.co.indivara.jdt12.najmi.nbank.service.AuthService;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

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
    private TrxCardlessRepo trxCardlessRepo;

    @Autowired
    private AuthService authService;


    @Before // jalanin setiap sebelum test
    public void setup(){
        objectMapper.registerModule(new JavaTimeModule());
        trxCardlessRepo.deleteAll();
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
     * 5. deposit cardless via atm - done
     *   5.1 failed, deposit id not found - done
     *   5.2 failed, deposit id is a withdraw id eventually - done
     *   5.3 failed, deposit already redeemed - done
     *   5.4 failed, deposit money is less than expected - done
     *   5.5 failed, deposit maximum is exceeded - done
     *
     * 6. withdraw cardless via atm - done
     *   6.1 failed, withdraw id not found - done
     *   6.2 failed, withdraw id is a deposit id eventually - done
     *   6.3 failed, withdraw already redeemed - done
     *   6.4 failed, withdraw maximum is exceeded - done
     *   6.5 failed, in sufficient balance to do redeem - done
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

        OnlyMoneyDepositWithdrawRequest request = OnlyMoneyDepositWithdrawRequest.builder()
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
        OnlyMoneyDepositWithdrawRequest request = OnlyMoneyDepositWithdrawRequest.builder()
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

        OnlyMoneyDepositWithdrawRequest request = OnlyMoneyDepositWithdrawRequest.builder()
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
        OnlyMoneyDepositWithdrawRequest request = OnlyMoneyDepositWithdrawRequest.builder()
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

    @Test
    public void depositRedeemedSuccess() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.DEPOSIT)
                        .redeemed(false)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<TrxDeposit, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TrxDeposit, NullType>>() {});
            Account accountRes = accountRepo.findById(account.getAccountId()).orElseThrow(null);
            Assertions.assertEquals(accountRes.getBalance().compareTo(account.getBalance().add(BigDecimal.valueOf(500_000))), 0);
            log.info(response.toString());
            Assertions.assertNotNull(response.getData());
            Assertions.assertNull(response.getError());
        });
    }

    @Test
    public void depositRedeemedFailedTicketIdNotFound() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(UUID.randomUUID())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Ticket Not found", response.getError().getName());
            Assertions.assertEquals("Ticket You Specified Could Not Be Found", response.getError().getDetail());
        });
    }

    @Test
    public void depositRedeemedFailedTicketIdIsAWithdrawTicket() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.WITHDRAW)
                        .redeemed(false)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Ticket Not Valid", response.getError().getName());
            Assertions.assertEquals("This Is A Withdraw ticket, Please Redeem It On Withdraw Feature", response.getError().getDetail());
        });
    }

    @Test
    public void depositRedeemedFailedTicketAlreadyRedeemed() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.DEPOSIT)
                        .redeemed(true)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .redeemedTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Ticket Redeemed", response.getError().getName());
            Assertions.assertTrue(((String) response.getError().getDetail()).startsWith("Ticket Is Already Redeemed at "));
        });
    }

    @Test
    public void depositRedeemedFailedMoneyNotFulfilTheRequirement() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.DEPOSIT)
                        .redeemed(false)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Money Violation", response.getError().getName());
            Assertions.assertEquals("Please Fulfil The Specified Amount Of money Requirements", response.getError().getDetail());
        });
    }

    @Test
    public void depositRedeemedFailedMaximumDepositIsExceeded() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.DEPOSIT)
                        .redeemed(false)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        trxDepositRepo.save(
                TrxDeposit.builder()
                        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                        .amount(BigDecimal.valueOf(35_000_000))
                        .account(account)
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Exceeding Amount", response.getError().getName());
            Assertions.assertEquals("You have Passed Daily Maximum DEPOSIT Transaction", response.getError().getDetail());
        });
    }

    @Test
    public void withdrawRedeemedSuccess() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0, BigDecimal.valueOf(50_000_000));

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.WITHDRAW)
                        .redeemed(false)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<TrxWithdraw, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TrxWithdraw, NullType>>() {});
            Account accountRes = accountRepo.findById(account.getAccountId()).orElseThrow(null);
            Assertions.assertEquals(accountRes.getBalance().compareTo(account.getBalance().subtract(BigDecimal.valueOf(500_000))), 0);
            log.info(response.toString());
            Assertions.assertNotNull(response.getData());
            Assertions.assertNull(response.getError());
        });
    }

    @Test
    public void withdrawRedeemedFailedTicketIdNotFound() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0, BigDecimal.valueOf(50_000_000));

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(UUID.randomUUID())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Ticket Not found", response.getError().getName());
            Assertions.assertEquals("Ticket You Specified Could Not Be Found", response.getError().getDetail());
        });
    }

    @Test
    public void withdrawRedeemedFailedTicketIdIsAdepositTicket() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.DEPOSIT)
                        .redeemed(false)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Ticket Not Valid", response.getError().getName());
            Assertions.assertEquals("This Is A Deposit ticket, Please Redeem It On Deposit Feature", response.getError().getDetail());
        });
    }

    @Test
    public void withdrawRedeemedFailedTicketAlreadyRedeemed() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.WITHDRAW)
                        .redeemed(true)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .redeemedTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Ticket Redeemed", response.getError().getName());
            Assertions.assertTrue(((String) response.getError().getDetail()).startsWith("Ticket Is Already Redeemed at "));
        });
    }

    @Test
    public void withdrawRedeemedFailedMaximumWithdrawIsExceeded() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0, BigDecimal.valueOf(21_000_000));

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.WITHDRAW)
                        .redeemed(false)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        trxWithdrawRepo.save(
                TrxWithdraw.builder()
                        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                        .amount(BigDecimal.valueOf(20_000_000))
                        .account(account)
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Exceeding Amount", response.getError().getName());
            Assertions.assertEquals("You have Passed Daily Maximum WITHDRAW Transaction", response.getError().getDetail());
        });
    }

    @Test
    public void withdrawRedeemedFailedMaximumInsufficient() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TrxCardless transaction = trxCardlessRepo.save(
                TrxCardless.builder()
                        .cardlessId(UUID.randomUUID())
                        .account(account)
                        .amount(BigDecimal.valueOf(500_000))
                        .type(CardLessEnum.WITHDRAW)
                        .redeemed(false)
                        .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                        .build()
        );

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest.builder()
                .uuid(transaction.getCardlessId())
                .money(BigDecimal.valueOf(500_000))
                .build();

        mockMvc.perform(
                post("/api/atm/redeem/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Balance Error", response.getError().getName());
            Assertions.assertEquals("Insufficient Balance To Complete This Transaction", response.getError().getDetail());
        });
    }
}
