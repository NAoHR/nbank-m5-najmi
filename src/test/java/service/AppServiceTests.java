package service;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxCardless;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.CardLessEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.model.request.OnlyMoneyDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.TrxCardlessRepo;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountHelper;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.AppServiceImpl;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.ValidatorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class AppServiceTests {

    @Mock
    private AccountHelper accountHelper;
    @Mock
    private ValidatorService validatorService;
    @Mock
    private TrxCardlessRepo trxCardlessRepo;

    @InjectMocks
    private AppServiceImpl appService;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void cardlessWithdrawTest(){
        Account account = Account
                .builder()
                .accountId(UUID.randomUUID())
                .customer(new Customer())
                .accountNumber("12345678")
                .password("waduhkumalala")
                .status(StatusEnum.ACTIVE)
                .accountType(AccountTypeEnum.SAVINGS)
                .balance(new BigDecimal("5000000"))
                .openDate(Timestamp.from(Instant.now()))
                .depositMonth(0)
                .build();

        OnlyMoneyDepositWithdrawRequest request = OnlyMoneyDepositWithdrawRequest
                .builder()
                .money(BigDecimal.valueOf(100_000L))
                .build();

        TrxCardless trxCardless = TrxCardless.builder()
                .cardlessId(UUID.randomUUID())
                .account(account)
                .amount(request.getMoney())
                .type(CardLessEnum.WITHDRAW)
                .redeemed(false)
                .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        doNothing().when(validatorService).validate(request);
        doNothing().when(accountHelper).checkAccountStatus(account);
        doNothing().when(accountHelper).checkAccountType(account);
        doNothing().when(accountHelper).multipleOf50kValidator(request.getMoney());
        doNothing().when(accountHelper).exceedingAmountOfTransaction(account, TransactionTypeEnum.WITHDRAW, request.getMoney());
        doNothing().when(accountHelper).checkAllowedMinimumBalance(account, account.getBalance().subtract(request.getMoney()));
        when(trxCardlessRepo.save(any(TrxCardless.class))).thenReturn(new TrxCardless());

        TrxCardless result = appService.cardlessWithdraw(account, request);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getCardlessId());
        Assertions.assertEquals(result.getAmount(), trxCardless.getAmount());
        Assertions.assertEquals(result.getType(), trxCardless.getType());
        Assertions.assertEquals(result.getRedeemed(), trxCardless.getRedeemed());

        verify(validatorService).validate(any());
        verify(accountHelper).checkAccountStatus(any());
        verify(accountHelper).checkAccountType(any());
        verify(accountHelper).multipleOf50kValidator(any());
        verify(accountHelper).exceedingAmountOfTransaction(any(), any(), any());
        verify(accountHelper).checkAllowedMinimumBalance(any(), any());
        verify(trxCardlessRepo).save(any());
        verifyNoMoreInteractions(validatorService, accountHelper, trxCardlessRepo);

    }

    @Test
    public void cardlessDeposit(){
        Account account = Account
                .builder()
                .accountId(UUID.randomUUID())
                .customer(new Customer())
                .accountNumber("12345678")
                .password("waduhkumalala")
                .status(StatusEnum.ACTIVE)
                .accountType(AccountTypeEnum.SAVINGS)
                .balance(new BigDecimal("5000000"))
                .openDate(Timestamp.from(Instant.now()))
                .depositMonth(0)
                .build();

        OnlyMoneyDepositWithdrawRequest request = OnlyMoneyDepositWithdrawRequest
                .builder()
                .money(BigDecimal.valueOf(100_000L))
                .build();

        TrxCardless trxCardless = TrxCardless.builder()
                .cardlessId(UUID.randomUUID())
                .account(account)
                .amount(request.getMoney())
                .type(CardLessEnum.DEPOSIT)
                .redeemed(false)
                .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        doNothing().when(validatorService).validate(request);
        doNothing().when(accountHelper).checkAccountStatus(account);
        doNothing().when(accountHelper).checkAccountType(account);
        doNothing().when(accountHelper).multipleOf50kValidator(request.getMoney());
        doNothing().when(accountHelper).exceedingAmountOfTransaction(account, TransactionTypeEnum.DEPOSIT, request.getMoney());
        when(trxCardlessRepo.save(any(TrxCardless.class))).thenReturn(new TrxCardless());

        TrxCardless result = appService.cardlessDeposit(account, request);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getCardlessId());
        Assertions.assertEquals(result.getAmount(), trxCardless.getAmount());
        Assertions.assertEquals(result.getType(), trxCardless.getType());
        Assertions.assertEquals(result.getRedeemed(), trxCardless.getRedeemed());

        verify(validatorService).validate(any());
        verify(accountHelper).checkAccountStatus(any());
        verify(accountHelper).checkAccountType(any());
        verify(accountHelper).multipleOf50kValidator(any());
        verify(accountHelper).exceedingAmountOfTransaction(any(), any(), any());
        verify(trxCardlessRepo).save(any());
        verifyNoMoreInteractions(validatorService, accountHelper, trxCardlessRepo);
    }

    @Test
    public void checkOwnedCardlessTransactionTest(){
        List<TrxCardless> lists = Arrays.asList(
                new TrxCardless(),
                new TrxCardless()
        );

        when(trxCardlessRepo.getTrxByOption(any(Account.class), anyBoolean(), any(CardLessEnum.class))).thenReturn(lists);
        when(trxCardlessRepo.getTrxByOption(any(Account.class),  any(CardLessEnum.class))).thenReturn(lists);

        List<TrxCardless> result = appService.checkOwnedCardlessTransaction(new Account(), "withdraw", "true");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.size(), 2);

        verify(trxCardlessRepo).getTrxByOption(any(), any(), any());
    }
}
