package id.co.indivara.jdt12.najmi.nbank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.co.indivara.jdt12.najmi.nbank.entity.*;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.HighestEducationEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.MaritalStatusEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.DepositRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.WithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterCustomerReq;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.TransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.OccuredError;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterAccountResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterCustomerResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.Before;

import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.lang.model.type.NullType;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

//import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AdminControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private TrxTransferRepo trxTransferRepo;
    @Autowired
    private TrxWithdrawRepo trxWithdrawRepo;

    @Autowired
    private TrxDepositRepo trxDepositRepo;

    @Autowired
    private TestHelper testHelper;

    @Before // jalanin setiap sebelum test
    public void setup(){
        objectMapper.registerModule(new JavaTimeModule());
        trxDepositRepo.deleteAll();
        trxWithdrawRepo.deleteAll();
        trxTransferRepo.deleteAll();
        accountRepo.deleteAll();
        customerRepo.deleteAll();
    }


    /*
     * 1. register sukses - done
     *  1.1 register failed - done
     *
     * 2. register account - done
     *  2.1 register failed (no customer) - done
     *  2.2 register failed (missing field) - done
     *  2.3 register failed (account with  money less than required) - done
     *
     * 3. display customer's accounts - done
     *  3.1 display customer's accounts failed - done
     *
     * 4. Withdraw - done
     *  4.1 withdraw failed because account not found - done
     *  4.2 withdraw failed because money not a multple of 50k - done
     *  4.3 withdraw failed because money in the bank below minimal balance - done
     *  4.4 withdraw failed because it's time deposit account - done
     *  4.5 withdraw failed because Exceeding transaction is passed - done
     *    4.5.1 saving - done
     *    4.5.1 checking - done
     *
     * 5. Deposit - done
     *  5.1 deposit failed because account not found - done
     *  5.2 deposit failed because money not a multple of 50k - done
     *  5.3 deposit failed because it's time deposit account - done
     *  5.4 deposit failed because exceeding transaction is passed - done
     *    5.4.1 saving - done
     *    5.4.2 checking - done
     *
     * 6. Transfer
     *  6.1 transfer failed because accountFrom not found
     *  6.2 transfer failed because destination not found
     *  6.3 transfer failed because money not a multiple of 5k
     *  6.4 transfer failed because accountFrom is a time deposit account
     *  6.7 transfer failed because destination is a time deposit account
     *  6.8 transfer failed because exceeding transaction is passed
     *    6.8.1 saving
     *    6.8.1 checking
     */
    @Test
    public void registerSuccessTest() throws Exception {

        RegisterCustomerReq customerReq = RegisterCustomerReq.builder()
                .identityNumber("1234567890123456")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("123456780")
                .email("john.doee@ample.com")
                .dob(LocalDate.parse("2004-07-20"))
                .address("123 Main Street")
                .occupation("Engineer")
                .income(new BigDecimal("50000.0"))
                .maritalStatus(MaritalStatusEnum.SINGLE)
                .highestEducation(HighestEducationEnum.BACHELORS_DEGREE)
                .password("Password123")
                .build();

        mockMvc.perform(
                post("/api/admin/registercustomer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerReq))
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<RegisterCustomerResponse, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<RegisterCustomerResponse, NullType>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNull(response.getError());
            log.info("passed no error to be shown");

            Assertions.assertNotNull(response.getData().getCustomerId());
            Assertions.assertEquals(customerReq.getIdentityNumber(), response.getData().getIdentityNumber());
            Assertions.assertEquals(customerReq.getFirstName(), response.getData().getFirstName());
            Assertions.assertEquals(customerReq.getLastName(), response.getData().getLastName());
            Assertions.assertEquals(customerReq.getPhoneNumber(), response.getData().getPhoneNumber());
            Assertions.assertEquals(customerReq.getEmail(), response.getData().getEmail());
            Assertions.assertEquals(customerReq.getDob(), response.getData().getDob());
            Assertions.assertEquals(customerReq.getAddress(), response.getData().getAddress());
            Assertions.assertEquals(customerReq.getIncome(), response.getData().getIncome());
            Assertions.assertEquals(customerReq.getOccupation(), response.getData().getOccupation());
            Assertions.assertEquals(customerReq.getMaritalStatus(), response.getData().getMaritalStatus());
            Assertions.assertEquals(customerReq.getHighestEducation(), response.getData().getHighestEducation());
            log.info("passed all requirement customer");
        });
    }

    @Test
    public void registerFailedTest() throws Exception{
        // failed with certain field (identity number, firstname,  phone number)
        RegisterCustomerReq customerReq = RegisterCustomerReq.builder()
                .email("john.doee@ample.com")
                .dob(LocalDate.parse("2004-07-20"))
                .address("123 Main Street")
                .occupation("Engineer")
                .income(new BigDecimal("50000.0"))
                .maritalStatus(MaritalStatusEnum.SINGLE)
                .highestEducation(HighestEducationEnum.BACHELORS_DEGREE)
                .password("Password123")
                .build();

        mockMvc.perform(
                post("/api/admin/registercustomer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerReq))
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType,Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType,Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            OccuredError<Object> err = response.getError();

            Assertions.assertEquals("Constraints Violations", err.getName());
            Assertions.assertEquals(3, ((List<String>) err.getDetail()).size());
        });
    }

    @Test
    public void registerAccountSuccessTest() throws Exception{
        Customer customer = testHelper.createOkCustomer();
        RegisterAccountRequest request = RegisterAccountRequest.builder()
                .accountPassword("password123")
                .status(StatusEnum.ACTIVE)
                .accountType(AccountTypeEnum.SAVINGS)
                .balance(new BigDecimal(500_000))
                .customerId(customer.getCustomerId())
                .depositMonth(0)
                .build();

        mockMvc.perform(
                post("/api/admin/registeraccount")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isCreated()
        ).andDo( result -> {
            WebResponse< RegisterAccountResponse, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<RegisterAccountResponse, NullType>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());

            Assertions.assertNotNull(response.getData().getAccountId());
            Assertions.assertNotNull(response.getData().getAccountNumber());

            Assertions.assertEquals(request.getStatus(), response.getData().getStatus());
            Assertions.assertEquals(request.getAccountType(), response.getData().getAccountType());
            Assertions.assertEquals(request.getBalance(), response.getData().getBalance());
            Assertions.assertEquals(request.getDepositMonth(), response.getData().getDepositMonth());
        });

    }

    @Test
    public void registerAccountFailedNoCustomerProvidedTest() throws Exception{
        Customer customer = testHelper.createOkCustomer();
        RegisterAccountRequest request = RegisterAccountRequest.builder()
                .accountPassword("password123")
                .status(StatusEnum.ACTIVE)
                .accountType(AccountTypeEnum.SAVINGS)
                .balance(new BigDecimal(500_000))
                .customerId(UUID.randomUUID()) // uuid random, bukan yang ada di database
                .depositMonth(12)
                .build();

        mockMvc.perform(
                post("/api/admin/registeraccount")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo( result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Customer Not Found", response.getError().getName());
        });

    }

    @Test
    public void registerAccountFailedMissingFieldTest() throws Exception{
        Customer customer = testHelper.createOkCustomer();
        RegisterAccountRequest request = RegisterAccountRequest.builder()
//                .accountPassword("password123")
                .status(StatusEnum.ACTIVE)
//                .accountType(AccountTypeEnum.SAVINGS)
                .balance(new BigDecimal(500_000))
                .customerId(customer.getCustomerId())
                .depositMonth(12)
                .build();

        mockMvc.perform(
                post("/api/admin/registeraccount")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo( result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());

        });

    }

    @Test
    public void registerAccountMoneyLessThanRequiredTest() throws Exception{
        Customer customer = testHelper.createOkCustomer();
        RegisterAccountRequest request = RegisterAccountRequest.builder()
                .accountPassword("password123")
                .status(StatusEnum.ACTIVE)
                .accountType(AccountTypeEnum.SAVINGS)
                .balance(new BigDecimal(50_000)) // should be 500k
                .customerId(customer.getCustomerId())
                .depositMonth(12)
                .build();

        mockMvc.perform(
                post("/api/admin/registeraccount")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo( result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());
        });

    }


    @Test
    public void displayCustomerAccountsTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        mockMvc.perform(
                get(String.format("/api/admin/customer/%s", account.getCustomer().getCustomerId().toString()))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isFound()
        ).andDo(result -> {
            WebResponse<DisplayCustomerAndAllAccountsResponse, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<DisplayCustomerAndAllAccountsResponse, NullType>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());
        });
    }

    @Test
    public void displayCustomerAccountsFailedTest() throws Exception{
        mockMvc.perform(
                get(String.format("/api/admin/customer/%s", UUID.randomUUID()))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());

        });
    }

    @Test
    public void displayAccountActivityTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        mockMvc.perform(
                get(String.format("/api/admin/customer/%s/transaction", account.getAccountId()))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isFound()
        ).andDo(result -> {
           WebResponse<HashMap<String, Object>, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<HashMap<String, Object>, NullType>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());
        });
    }

    @Test
    public void displayAccountActivityFailedTest() throws Exception{
        mockMvc.perform(
                get(String.format("/api/admin/customer/%s/transaction", UUID.randomUUID()))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
        });
    }


    @Test
    public void withdrawSuccessTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        WithdrawRequest transferRequest = WithdrawRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(50_000))
                .build();
        mockMvc.perform(
                post("/api/admin/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isCreated()
        ).andDo(result -> {
            WebResponse< TrxWithdraw, NullType > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TrxWithdraw, NullType>>() {});
            log.info(String.valueOf(response));
            Account accountAfter = accountRepo.findById(account.getAccountId()).orElseThrow(null);

            Assertions.assertEquals(accountAfter.getBalance(), account.getBalance().subtract(BigDecimal.valueOf(50_000)).setScale(2));
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());
        });
    }

    @Test
    public void WithdrawFailedAccountNotFoundTest() throws Exception{
        WithdrawRequest transferRequest = WithdrawRequest.builder()
                .uid(UUID.randomUUID())
                .money(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));

            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            log.info(String.valueOf(response));
        });
    }

    @Test
    public void withdrawFailedNotMultpleOf50k() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        WithdrawRequest transferRequest = WithdrawRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(53_000))
                .build();
        mockMvc.perform(
                post("/api/admin/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            log.info(String.valueOf(response));
        });
    }

    @Test
    public void withdrawFailedMinimumMoneyInBalanceTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        WithdrawRequest transferRequest = WithdrawRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(500_000))
                .build();
        mockMvc.perform(
                post("/api/admin/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            log.info(String.valueOf(response));
        });
    }

    @Test
    public void withdrawFailedToTimeDepositTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.TIME_DEPOSIT, 12);
        WithdrawRequest transferRequest = WithdrawRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(500_000))
                .build();
        mockMvc.perform(
                post("/api/admin/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void withdrawFailedExceedingTransactionIsPassedSavings() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 12, BigDecimal.valueOf(20_000_000));

        // to fulfil the exceeded money so the request will get an error
        TrxWithdraw trxWithdraw = TrxWithdraw.builder()
                .account(account)
                .amount(BigDecimal.valueOf(10_000_000))
                .account(account)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        trxWithdrawRepo.save(trxWithdraw);

        WithdrawRequest witdrawRequest = WithdrawRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(1_000_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(witdrawRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            log.info(String.valueOf(response));
        });
    }

    @Test
    public void withdrawFailedExceedingTransactionIsPassedChecking() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.CHECKING, 12, BigDecimal.valueOf(22_000_000));

        // to fulfil the exceeded money so the request will get an error
        TrxWithdraw trxWithdraw = TrxWithdraw.builder()
                .account(account)
                .amount(BigDecimal.valueOf(20_000_000))
                .account(account)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        trxWithdrawRepo.save(trxWithdraw);

        WithdrawRequest witdrawRequest = WithdrawRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(1_000_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(witdrawRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            log.info(String.valueOf(response));
        });
    }

    @Test
    public void depositSuccessTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        DepositRequest depositRequest = DepositRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(50_000))
                .build();
        mockMvc.perform(
                post("/api/admin/transaction/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(depositRequest))
        ).andExpectAll(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<TrxDeposit, NullType > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TrxDeposit, NullType>>() {});
            log.info(String.valueOf(response));
            Account accountAfter = accountRepo.findById(account.getAccountId()).orElseThrow(null);

            Assertions.assertEquals(accountAfter.getBalance(), account.getBalance().add(BigDecimal.valueOf(50_000)).setScale(2));
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());
        });
    }

    @Test
    public void depositFailedAccountNotFoundTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        DepositRequest depositRequest = DepositRequest.builder()
                .uid(UUID.randomUUID())
                .money(BigDecimal.valueOf(50_000))
                .build();
        mockMvc.perform(
                post("/api/admin/transaction/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(depositRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Account Not Found", response.getError().getName());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void depositFailedAccountNotAMultipleOf50KTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        DepositRequest depositRequest = DepositRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(55_000))
                .build();
        mockMvc.perform(
                post("/api/admin/transaction/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(depositRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Money Error", response.getError().getName());
            Assertions.assertEquals("Money Needs to Be a Multiple Of Rp 50.000", response.getError().getDetail());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void depositFailedToTimeDepositAccountTest() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.TIME_DEPOSIT, 12);
        DepositRequest depositRequest = DepositRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(50_000))
                .build();
        mockMvc.perform(
                post("/api/admin/transaction/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(depositRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Time Deposit Violation", response.getError().getName());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void depositFailedExceedingTransactionIsPassedSavings() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 12);

        // payload to exceed the maximum deposit
        TrxDeposit trxDeposit = TrxDeposit.builder()
                .account(account)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .amount(BigDecimal.valueOf(35_000_000))
                .build();

        trxDepositRepo.save(trxDeposit);

        DepositRequest depositRequest = DepositRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(depositRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Exceeding Amount", response.getError().getName());
            Assertions.assertEquals("You have Passed Daily Maximum DEPOSIT Transaction", response.getError().getDetail());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void depositFailedExceedingTransactionIsPassedChecking() throws Exception{
        Account account = testHelper.createOkAccount(AccountTypeEnum.CHECKING, 12);

        // payload to exceed the maximum deposit
        TrxDeposit trxDeposit = TrxDeposit.builder()
                .account(account)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .amount(BigDecimal.valueOf(15_000_000))
                .build();

        trxDepositRepo.save(trxDeposit);

        DepositRequest depositRequest = DepositRequest.builder()
                .uid(account.getAccountId())
                .money(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/deposit")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(depositRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse< NullType, Object > response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});
            log.info(String.valueOf(response));

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Exceeding Amount", response.getError().getName());
            Assertions.assertEquals("You have Passed Daily Maximum DEPOSIT Transaction", response.getError().getDetail());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void transferSuccess() throws Exception{
        Account accountFrom = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        Account accountTo = testHelper.createOkAccount(AccountTypeEnum.CHECKING, 0);

        TransferRequest transferRequest = TransferRequest.builder()
                .from(accountFrom.getAccountId())
                .destination(accountTo.getAccountId())
                .amount(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isCreated()
        ).andDo(result -> {
            WebResponse<TrxTransferReferencedId, NullType> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TrxTransferReferencedId, NullType>>() {});
            Account resAccountFrom = accountRepo.findById(accountFrom.getAccountId()).orElseThrow(null);
            Account resAccountTo = accountRepo.findById(accountTo.getAccountId()).orElseThrow(null);

            Assertions.assertEquals(accountFrom.getBalance().subtract(BigDecimal.valueOf(50_000)).setScale(2), resAccountFrom.getBalance());
            Assertions.assertEquals(accountTo.getBalance().add(BigDecimal.valueOf(50_000)).setScale(2), resAccountTo.getBalance());

            log.info(response.toString());
            Assertions.assertNotNull(response.getData());
            Assertions.assertNull(response.getError());
        });

    }

    @Test
    public void transferFailedAccountFromNotFound() throws Exception{
        Account accountTo = testHelper.createOkAccount(AccountTypeEnum.CHECKING, 0);

        TransferRequest transferRequest = TransferRequest.builder()
                .from(UUID.randomUUID())
                .destination(accountTo.getAccountId())
                .amount(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});

            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());

            Assertions.assertEquals("Account Not Found", response.getError().getName());
        });
    }

    @Test
    public void transferFailedAccountToNotFound() throws Exception{
        Account accountFrom = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);

        TransferRequest transferRequest = TransferRequest.builder()
                .from(accountFrom.getAccountId())
                .destination(UUID.randomUUID())
                .amount(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});

            log.info(response.toString());
            Assertions.assertNull(response.getData());
            Assertions.assertNotNull(response.getError());

            Assertions.assertEquals("Account Not Found", response.getError().getName());
        });
    }

    @Test
    public void transferFailedNotAMultpleOf5k() throws Exception{
        Account accountFrom = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        Account accountTo = testHelper.createOkAccount(AccountTypeEnum.CHECKING, 0);

        TransferRequest transferRequest = TransferRequest.builder()
                .from(accountFrom.getAccountId())
                .destination(accountTo.getAccountId())
                .amount(BigDecimal.valueOf(52_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});

            log.info(response.toString());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Money Error", response.getError().getName());
            Assertions.assertEquals("Money Needs to Be a Multiple Of Rp 50.000", response.getError().getDetail());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void transferFailedAccountFromIsTimeDeposit() throws Exception{
        Account accountFrom = testHelper.createOkAccount(AccountTypeEnum.TIME_DEPOSIT, 12);
        Account accountTo = testHelper.createOkAccount(AccountTypeEnum.CHECKING, 0);

        TransferRequest transferRequest = TransferRequest.builder()
                .from(accountFrom.getAccountId())
                .destination(accountTo.getAccountId())
                .amount(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});

            log.info(response.toString());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Time Deposit Violation", response.getError().getName());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void transferFailedAccountToIsTimeDeposit() throws Exception{
        Account accountFrom = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0);
        Account accountTo = testHelper.createOkAccount(AccountTypeEnum.TIME_DEPOSIT, 12);

        TransferRequest transferRequest = TransferRequest.builder()
                .from(accountFrom.getAccountId())
                .destination(accountTo.getAccountId())
                .amount(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});

            log.info(response.toString());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Time Deposit Violation", response.getError().getName());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void transferFailedExceedingMaximumMoneySavingTest() throws Exception{
        Account accountFrom = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 0, BigDecimal.valueOf(15_000_000));
        Account accountTo = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 12);

        TrxTransfer trxTransfer = TrxTransfer.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .account(accountFrom)
                .destination(accountTo)
                .amount(BigDecimal.valueOf(15_000_000))
                .build();

        trxTransferRepo.save(trxTransfer);

        TransferRequest transferRequest = TransferRequest.builder()
                .from(accountFrom.getAccountId())
                .destination(accountTo.getAccountId())
                .amount(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});

            log.info(response.toString());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Exceeding Amount", response.getError().getName());
            Assertions.assertNull(response.getData());
        });
    }

    @Test
    public void transferFailedExceedingMaximumMoneyCheckingTest() throws Exception{
        Account accountFrom = testHelper.createOkAccount(AccountTypeEnum.CHECKING, 0, BigDecimal.valueOf(20_000_000));
        Account accountTo = testHelper.createOkAccount(AccountTypeEnum.SAVINGS, 12);

        TrxTransfer trxTransfer = TrxTransfer.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .account(accountFrom)
                .destination(accountTo)
                .amount(BigDecimal.valueOf(20_000_000))
                .build();

        trxTransferRepo.save(trxTransfer);

        TransferRequest transferRequest = TransferRequest.builder()
                .from(accountFrom.getAccountId())
                .destination(accountTo.getAccountId())
                .amount(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(
                post("/api/admin/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("nbankadmin:password".getBytes()))
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<NullType, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<NullType, Object>>() {});

            log.info(response.toString());
            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("Exceeding Amount", response.getError().getName());
            Assertions.assertNull(response.getData());
        });
    }

}