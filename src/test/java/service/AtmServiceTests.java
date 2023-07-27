package service;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxCardless;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.enums.CardLessEnum;
import id.co.indivara.jdt12.najmi.nbank.model.RedeemWithdrawOrDepositRequest;
import id.co.indivara.jdt12.najmi.nbank.repo.TrxCardlessRepo;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.AtmServiceImpl;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.ValidatorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class AtmServiceTests {
    @Mock
    private ValidatorService validatorService;
    @Mock
    private AccountService accountService;
    @Mock
    private TrxCardlessRepo trxCardlessRepo;

    @InjectMocks
    private AtmServiceImpl atmService;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void redeemDepositTest(){
        BigDecimal money = new BigDecimal("50000");

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest
                .builder()
                .uuid(UUID.randomUUID())
                .money(money)
                .build();

        TrxCardless trxCardless = TrxCardless.builder()
                .createdTime(Timestamp.from(Instant.now()))
                .type(CardLessEnum.DEPOSIT)
                .redeemed(false)
                .amount(money)
                .account(new Account())
                .build();

        TrxDeposit depositResponse = TrxDeposit
                .builder()
                .depositId(12345L)
                .account(new Account())
                .timestamp(Timestamp.from(Instant.now()))
                .amount(money)
                .build();

        doNothing().when(validatorService).validate(request);
        when(trxCardlessRepo.findById(any())).thenReturn(Optional.of(trxCardless));
        when(trxCardlessRepo.save(trxCardless)).thenReturn(trxCardless);
        when(accountService.deposit(trxCardless.getCardlessId(), money, true)).thenReturn(depositResponse);

        TrxDeposit response = atmService.redeemDeposit(request);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getAmount(), money);

        verify(validatorService).validate(any());
        verify(trxCardlessRepo).findById(any());
        verify(trxCardlessRepo).save(any());
        verify(accountService).deposit(trxCardless.getCardlessId(), money, true);
        verifyNoMoreInteractions(validatorService, trxCardlessRepo, accountService);
    }

    @Test
    public void redeemWithdrawTest(){
        BigDecimal money = new BigDecimal("50000");

        RedeemWithdrawOrDepositRequest request = RedeemWithdrawOrDepositRequest
                .builder()
                .uuid(UUID.randomUUID())
                .money(money)
                .build();

        TrxCardless trxCardless = TrxCardless.builder()
                .createdTime(Timestamp.from(Instant.now()))
                .type(CardLessEnum.WITHDRAW)
                .redeemed(false)
                .amount(money)
                .account(new Account())
                .build();

        TrxWithdraw witdrawResponse = TrxWithdraw
                .builder()
                .withdrawId(12345L)
                .account(new Account())
                .timestamp(Timestamp.from(Instant.now()))
                .amount(money)
                .build();

        doNothing().when(validatorService).validate(request);
        when(trxCardlessRepo.findById(any())).thenReturn(Optional.of(trxCardless));
        when(trxCardlessRepo.save(trxCardless)).thenReturn(trxCardless);
        when(accountService.withdraw(trxCardless.getCardlessId(), money, true)).thenReturn(witdrawResponse);

        TrxWithdraw response = atmService.redeemWithdraw(request);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getAmount(), money);

        verify(validatorService).validate(any());
        verify(trxCardlessRepo).findById(any());
        verify(trxCardlessRepo).save(any());
        verify(accountService).withdraw(trxCardless.getCardlessId(), money, true);
        verifyNoMoreInteractions(validatorService, trxCardlessRepo, accountService);
    }
}
