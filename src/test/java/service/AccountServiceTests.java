package service;

import id.co.indivara.jdt12.najmi.nbank.entity.*;
import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountHelper;
import id.co.indivara.jdt12.najmi.nbank.service.serviceimpl.AccountServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;

public class AccountServiceTests {
    @Mock
    private AccountRepo accountRepo;

    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private TrxWithdrawRepo trxWithdrawRepo;

    @Mock
    private TrxDepositRepo trxDepositRepo;

    @Mock
    private TrxTransferRepo trxTransferRepo;
    @Mock
    private AccountHelper accountHelper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Before
    public void setup(){
        System.out.println("here");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void displayAccountTransactionActivityTest(){
        TrxTransfer transfer = TrxTransfer
                .builder()
                .account(new Account())
                .destination(new Account())
                .build();

        List<TrxTransfer> transferList = Arrays.asList(
                transfer
        );


        when(trxTransferRepo.findAllByAccount(any())).thenReturn(transferList);
        when(trxDepositRepo.findAllByAccount(any())).thenReturn(new ArrayList<>());
        when(trxWithdrawRepo.findAllByAccount(any())).thenReturn(new ArrayList<>());

        HashMap<String, Object> result = accountService.displayAccountTransactionActivity(new Account(), "all");
        Assertions.assertNotNull(result);

        verify(trxDepositRepo, times(1)).findAllByAccount(any(Account.class));
        verify(trxWithdrawRepo, times(1)).findAllByAccount(any(Account.class));
        verify(trxTransferRepo, times(1)).findAllByAccount(any(Account.class));
        verify(accountHelper, times(1)).transferActivity(transferList);
    }

    @Test
    public void displayCustomerAndAllAccountsTest(){
        when(customerRepo.findByEmail(anyString())).thenReturn(Optional.of(new Customer()));
        when(accountRepo.findAllByCustomer(any(Customer.class))).thenReturn(new ArrayList<>());

        DisplayCustomerAndAllAccountsResponse result = accountService.displayCustomerAndAllAccounts("email@dot.com");

        Assertions.assertNotNull(result);

        verify(customerRepo).findByEmail(anyString());
        verify(accountRepo).findAllByCustomer(any(Customer.class));
    }

    @Test
    public void testDepositForAdmin() {
        // Mock data
        Account account = new Account();
        account.setStatus(StatusEnum.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        BigDecimal amount = new BigDecimal("50000");
        boolean isBankOrAdmin = true;

        // Mock behavior of accountHelper methods
        doNothing().when(accountHelper).checkAccountStatus(any(Account.class));
        doNothing().when(accountHelper).checkAccountType(any(Account.class));
        doNothing().when(accountHelper).multipleOf50kValidator(any(BigDecimal.class));
        doNothing().when(accountHelper).exceedingAmountOfTransaction(any(Account.class), any(TransactionTypeEnum.class), any(BigDecimal.class));

        // Mock behavior of repositories
        when(accountRepo.save(any(Account.class))).thenReturn(account);
        when(trxDepositRepo.save(any(TrxDeposit.class))).thenReturn(new TrxDeposit());

        // Call the method to be tested
        TrxDeposit result = accountService.deposit(account, amount, isBankOrAdmin);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(amount, result.getAmount());

        // Assertions
        Assertions.assertNotNull(result);
        Assertions.assertEquals(amount, account.getBalance());

        // Verify method calls
        verify(accountHelper).checkAccountStatus(account);
        verify(accountHelper).checkAccountType(account);
        verify(accountHelper).multipleOf50kValidator(amount);
        verify(accountHelper).exceedingAmountOfTransaction(account, TransactionTypeEnum.DEPOSIT, amount);
        verify(accountRepo).save(account);
        verify(trxDepositRepo).save(any(TrxDeposit.class));
    }

    @Test
    public void withdrawTest(){
        // Mock data
        Account account = new Account();
        account.setStatus(StatusEnum.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        BigDecimal amount = new BigDecimal("50000");
        boolean isBankOrAdmin = true;

        // Mock behaviour
        doNothing().when(accountHelper).checkAccountStatus(any(Account.class));
        doNothing().when(accountHelper).checkAccountType(any(Account.class));
        doNothing().when(accountHelper).multipleOf50kValidator(any(BigDecimal.class));
        doNothing().when(accountHelper).exceedingAmountOfTransaction(any(Account.class), any(TransactionTypeEnum.class), any(BigDecimal.class));
        doNothing().when(accountHelper).checkAllowedMinimumBalance(any(Account.class), any(BigDecimal.class));

        // Mock behavior of repositories
        when(accountRepo.save(any(Account.class))).thenReturn(account);
        when(trxWithdrawRepo.save(any(TrxWithdraw.class))).thenReturn(new TrxWithdraw());

        // call method
        TrxWithdraw result = accountService.withdraw(account, amount, isBankOrAdmin);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(amount, result.getAmount());

        verify(accountHelper).checkAccountStatus(account);
        verify(accountHelper).checkAccountType(account);
        verify(accountHelper).multipleOf50kValidator(amount);
        verify(accountHelper).exceedingAmountOfTransaction(account, TransactionTypeEnum.WITHDRAW, amount);
        verify(accountRepo).save(account);
        verify(trxWithdrawRepo).save(any(TrxWithdraw.class));
    }

    @Test
    public void transferWithTest(){
        Account accountFrom = new Account();
        accountFrom.setAccountNumber("12345");
        accountFrom.setStatus(StatusEnum.ACTIVE);
        accountFrom.setBalance(new BigDecimal("100000000"));

        Account accountTo = new Account();
        accountFrom.setAccountNumber("123456");
        accountTo.setStatus(StatusEnum.ACTIVE);
        accountTo.setBalance(BigDecimal.ZERO);

        BigDecimal amount = new BigDecimal("50000");
        boolean isBankOrAdmin = true;

        TrxTransfer response = TrxTransfer.builder()
                .account(accountFrom)
                .destination(accountTo)
                .amount(amount)
                .build();

        when(accountRepo.findByAccountNumber(accountTo.getAccountNumber())).thenReturn(Optional.of(accountTo));
        doNothing().when(accountHelper).checkAccountStatus(any(Account.class));
        doNothing().when(accountHelper).checkAccountType(any(Account.class));
        doNothing().when(accountHelper).multipleOf50kValidator(any(BigDecimal.class));
        doNothing().when(accountHelper).checkAllowedMinimumBalance(any(Account.class), any(BigDecimal.class));
        doNothing().when(accountHelper).exceedingAmountOfTransaction(any(Account.class), any(TransactionTypeEnum.class), any(BigDecimal.class));

        when(accountRepo.save(any(Account.class))).thenReturn(accountFrom).thenReturn(accountTo);
        when(trxTransferRepo.save(any(TrxTransfer.class))).thenReturn(any(TrxTransfer.class));

        TrxTransferReferencedId result = accountService.transfer(accountFrom, accountTo.getAccountNumber(), amount, isBankOrAdmin);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(response.getAmount(), result.getAmount());

        verify(accountRepo).findByAccountNumber(accountTo.getAccountNumber());
        verify(accountHelper, times(2)).checkAccountStatus(any(Account.class));
        verify(accountHelper, times(2)).checkAccountType(any(Account.class));

        verify(accountHelper).multipleOf50kValidator(amount);
        verify(accountHelper).checkAllowedMinimumBalance(accountFrom, accountFrom.getBalance());
        verify(accountHelper).exceedingAmountOfTransaction(accountFrom, TransactionTypeEnum.TRANSFER, amount);

        verify(accountRepo, times(2)).save(any(Account.class));
        verify(trxTransferRepo).save(any(TrxTransfer.class));
        verifyNoMoreInteractions(accountRepo, trxTransferRepo, accountHelper);
    }
}
