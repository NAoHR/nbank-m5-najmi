package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.*;
import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.exception.AccountNotFoundException;
import id.co.indivara.jdt12.najmi.nbank.exception.CustomerNotFoundException;
import id.co.indivara.jdt12.najmi.nbank.exception.ValidActivityException;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepo accountRepo;
    private final CustomerRepo customerRepo;
    private final TrxWithdrawRepo trxWithdrawRepo;
    private final TrxDepositRepo trxDepositRepo;
    private final TrxTransferRepo trxTransferRepo;
    private final AccountHelper accountHelper;


    @Override
    public HashMap<String, Object> displayAccountTransactionActivity(String accountNumber, String transactionType) {
        Account account = accountRepo.findByAccountNumber(accountNumber).orElseThrow(AccountNotFoundException::new);
        return displayAccountTransactionActivity(account, transactionType);
    }


    @Override
    public HashMap<String, Object> displayAccountTransactionActivity(Account account, String transactionType) {
        HashMap<String, Object> maps = new HashMap<>();
        HashMap<String, Object> subMap = new HashMap<>();
        maps.put("account", account);

        switch (transactionType.toLowerCase()){
            case "transfer":
                subMap.put("transfer", accountHelper.transferActivity(trxTransferRepo.findAllByAccount(account)));
                break;
            case "deposit":
                subMap.put("deposit", trxDepositRepo.findAllByAccount(account));
                break;
            case "withdraw":
                subMap.put("withdraw", trxWithdrawRepo.findAllByAccount(account));
                break;
            case "all":
                subMap.put("deposit", trxDepositRepo.findAllByAccount(account));
                subMap.put("withdraw", trxWithdrawRepo.findAllByAccount(account));
                subMap.put("transfer", accountHelper.transferActivity(trxTransferRepo.findAllByAccount(account)));
                break;
            default:
                throw new ValidActivityException();
        }
        maps.put("transaction", subMap);

        return maps;
    }

    @Override
    public DisplayCustomerAndAllAccountsResponse displayCustomerAndAllAccounts(String customerEmail) {
        Customer customer = customerRepo.findByEmail(customerEmail).orElseThrow(CustomerNotFoundException::new);
        List<Account> accounts = accountRepo.findAllByCustomer(customer);

        return DisplayCustomerAndAllAccountsResponse.builder()
                .customer(customer)
                .accounts(accounts)
                .build();
    }

    @Override
    public TrxDeposit deposit(UUID accountId, BigDecimal amount, boolean isBankOrAdmin) {
        Account account = accountRepo.findById(accountId).orElseThrow(AccountNotFoundException::new);

        return deposit(account, amount, isBankOrAdmin);
    }

    @Override
    @Transactional
    public TrxDeposit deposit(Account account, BigDecimal amount, boolean isBankOrAdmin) {

        accountHelper.checkAccountStatus(account);
        accountHelper.checkAccountType(account);

        if(isBankOrAdmin) accountHelper.multipleOf50kValidator(amount);

        accountHelper.exceedingAmountOfTransaction(account, TransactionTypeEnum.DEPOSIT, amount);

        account.setBalance(account.getBalance().add(amount));

        TrxDeposit trxDeposit = TrxDeposit.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .amount(amount)
                .account(account)
                .build();


        accountRepo.save(account);
        trxDepositRepo.save(trxDeposit);
        return trxDeposit;
    }



    @Override
    public TrxWithdraw withdraw(UUID accountId, BigDecimal amount, boolean isBankOrAdmin) {

        Account account = accountRepo.findById(accountId).orElseThrow(AccountNotFoundException::new);

        return withdraw(account, amount, isBankOrAdmin);
    }

    @Override
    @Transactional
    public TrxWithdraw withdraw(Account account, BigDecimal amount, boolean isBankOrAdmin) {

        accountHelper.checkAccountStatus(account);
        accountHelper.checkAccountType(account);

        if(isBankOrAdmin) accountHelper.multipleOf50kValidator(amount);
        accountHelper.exceedingAmountOfTransaction(account, TransactionTypeEnum.WITHDRAW, amount);

        BigDecimal nowMoney = account.getBalance().subtract(amount);
        accountHelper.checkAllowedMinimumBalance(account, nowMoney);

        TrxWithdraw trxWithdraw = TrxWithdraw.builder()
                .amount(amount)
                .account(account)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        account.setBalance(nowMoney);

        accountRepo.save(account);
        trxWithdrawRepo.save(trxWithdraw);

        return trxWithdraw;
    }

    @Override
    public TrxTransferReferencedId transfer(String from, String to, BigDecimal money, boolean isBankOrAdmin) {
        Account accountFrom = accountRepo.findByAccountNumber(from).orElseThrow(AccountNotFoundException::new);
        return transfer(accountFrom, to, money, isBankOrAdmin);
    }

    @Override
    @Transactional
    public TrxTransferReferencedId transfer(Account accountFrom, String to, BigDecimal money, boolean isBankOrAdmin) {
        if(accountFrom.getAccountNumber().equals(to)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Transfer : Can't transfer To Your Own Self");
        }

        Account accountTo = accountRepo.findByAccountNumber(to).orElseThrow(AccountNotFoundException::new);

        accountHelper.checkAccountStatus(accountFrom);
        accountHelper.checkAccountStatus(accountTo);

        accountHelper.checkAccountType(accountFrom);
        accountHelper.checkAccountType(accountTo);
        if(isBankOrAdmin) accountHelper.multipleOf50kValidator(money);

        BigDecimal accountFromMoney = accountFrom.getBalance().subtract(money);

        accountHelper.checkAllowedMinimumBalance(accountFrom, accountFromMoney);
        accountHelper.exceedingAmountOfTransaction(accountFrom, TransactionTypeEnum.TRANSFER, money);

        BigDecimal accountToMoney = accountTo.getBalance().add(money);

        accountFrom.setBalance(accountFromMoney);
        accountTo.setBalance(accountToMoney);

        TrxTransfer trxTransfer = TrxTransfer.builder()
                .account(accountFrom)
                .amount(money)
                .destination(accountTo)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        accountRepo.save(accountFrom);
        accountRepo.save(accountTo);
        trxTransferRepo.save(trxTransfer);

        return TrxTransferReferencedId.builder()
                .account(trxTransfer.getAccount().getAccountNumber())
                .transferId(trxTransfer.getTransferId())
                .amount(trxTransfer.getAmount())
                .destination(trxTransfer.getDestination().getAccountNumber())
                .timestamp(trxTransfer.getTimestamp())
                .build();
    }
}
