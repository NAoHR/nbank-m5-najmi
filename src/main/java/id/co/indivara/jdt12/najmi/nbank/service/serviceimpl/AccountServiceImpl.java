package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.*;
import id.co.indivara.jdt12.najmi.nbank.exception.AccountNotFoundException;
import id.co.indivara.jdt12.najmi.nbank.exception.CustomerNotFoundException;
import id.co.indivara.jdt12.najmi.nbank.exception.ValidActivityException;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private TrxWithdrawRepo trxWithdrawRepo;
    @Autowired
    private TrxDepositRepo trxDepositRepo;
    @Autowired
    private TrxTransferRepo trxTransferRepo;

    @Autowired
    private AccountHelper accountHelper;


    @Override
    public HashMap<String, Object> displayAccountTransactionActivity(UUID accountId, String transactionType) {
        Account account = accountRepo.findById(accountId).orElseThrow(AccountNotFoundException::new);
        HashMap<String, Object> maps = new HashMap<>();
        switch (transactionType.toLowerCase()){
            case "transfer":

                maps.put("transfer", accountHelper.transferActivity(trxTransferRepo.findAllByAccount(account)));
                break;
            case "deposit":
                maps.put("deposit", trxDepositRepo.findAllByAccount(account));
                break;
            case "withdraw":
                maps.put("withdraw", trxWithdrawRepo.findAllByAccount(account));
                break;
            case "all":
                maps.put("deposit", trxDepositRepo.findAllByAccount(account));
                maps.put("withdraw", trxWithdrawRepo.findAllByAccount(account));
                maps.put("transfer", accountHelper.transferActivity(trxTransferRepo.findAllByAccount(account)));
                break;
            default:
                throw new ValidActivityException();
        }

        return maps;
    }

    @Override
    public DisplayCustomerAndAllAccountsResponse displayCustomerAndAllAccounts(UUID customerId) {
        Customer customer = customerRepo.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        List<Account> accounts = accountRepo.findAllByCustomer(customer);

        return DisplayCustomerAndAllAccountsResponse.builder()
                .customer(customer)
                .accounts(accounts)
                .build();
    }

    @Override
    public TrxDeposit deposit(UUID accountId, BigDecimal amount, boolean isBankOrAdmin) {

        Account account = accountRepo.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        accountHelper.checkAccountType(account);
        if(isBankOrAdmin) accountHelper.checkMoneyWithBankOrAdmin(amount);

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
        Account account = accountRepo.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        accountHelper.checkAccountType(account);
        if(isBankOrAdmin) accountHelper.checkMoneyWithBankOrAdmin(amount);

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
    public TrxTransferReferencedId transfer(UUID from, UUID to, BigDecimal money, boolean isBankOrAdmin) {
        if(from.equals(to)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Transfer : Can't transfer To Your Own Self");
        }
        Account accountFrom = accountRepo.findById(from).orElseThrow(AccountNotFoundException::new);
        Account accountTo = accountRepo.findById(to).orElseThrow(AccountNotFoundException::new);


        accountHelper.checkAccountType(accountFrom);
        accountHelper.checkAccountType(accountTo);
        if(isBankOrAdmin) accountHelper.checkMoneyWithBankOrAdmin(money);

        BigDecimal accountFromMoney = accountFrom.getBalance().subtract(money);

        accountHelper.checkAllowedMinimumBalance(accountFrom, accountFromMoney);

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
