package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;

import id.co.indivara.jdt12.najmi.nbank.entity.TrxCardless;
import id.co.indivara.jdt12.najmi.nbank.enums.CardLessEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.OnlyMoneyDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;

import id.co.indivara.jdt12.najmi.nbank.repo.TrxCardlessRepo;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.AppService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private TrxCardlessRepo trxCardlessRepo;


    @Override
    public final Customer showDetailCustomer(Customer c) {
        return c;
    }

    @Override
    public final List<Account> showCustomerAccount(Customer c) {
        return accountRepo.findAllByCustomer(c);
    }

    @Override
    public final HashMap<String, Object> showAccountDetailAndTransaction(Account account, String type) {
        return accountService.displayAccountTransactionActivity(account, type);
    }

    @Override
    public final TrxTransferReferencedId transferWithApp(Account c, AtmAndAppTransferRequest t) {
        validatorService.validate(t);
        return accountService.transfer(c, t.getDestination(), t.getAmount(), false);
    }

    @Override
    public TrxCardless cardlessWithdraw(Account account, OnlyMoneyDepositWithdrawRequest request) {
        validatorService.validate(request);

        accountHelper.checkAccountStatus(account);
        accountHelper.checkAccountType(account);
        accountHelper.multipleOf50kValidator(request.getMoney());
        accountHelper.exceedingAmountOfTransaction(account, TransactionTypeEnum.WITHDRAW, request.getMoney());

        BigDecimal nowMoney = account.getBalance().subtract(request.getMoney());
        accountHelper.checkAllowedMinimumBalance(account, nowMoney);

        TrxCardless trxCardless = TrxCardless.builder()
                .cardlessId(UUID.randomUUID())
                .account(account)
                .amount(request.getMoney())
                .type(CardLessEnum.WITHDRAW)
                .redeemed(false)
                .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        trxCardlessRepo.save(trxCardless);

        return trxCardless;
    }

    @Override
    public TrxCardless cardlessDeposit(Account account, OnlyMoneyDepositWithdrawRequest request) {
        validatorService.validate(request);

        accountHelper.checkAccountStatus(account);
        accountHelper.checkAccountType(account);
        accountHelper.multipleOf50kValidator(request.getMoney());
        accountHelper.exceedingAmountOfTransaction(account, TransactionTypeEnum.DEPOSIT, request.getMoney());

        TrxCardless trxCardless = TrxCardless.builder()
                .cardlessId(UUID.randomUUID())
                .account(account)
                .amount(request.getMoney())
                .type(CardLessEnum.DEPOSIT)
                .redeemed(false)
                .createdTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        trxCardlessRepo.save(trxCardless);

        return trxCardless;
    }

    @Override
    public List<TrxCardless> checkOwnedCardlessTransaction(Account account, String type, String redeemed) {
        ArrayList<TrxCardless> buckets;
        switch (type.toLowerCase()){
            case "withdraw":
                buckets = getTrxCardlessBasedOnOption(account, redeemed, CardLessEnum.WITHDRAW);
                break;
            case "deposit":
                buckets = getTrxCardlessBasedOnOption(account, redeemed, CardLessEnum.DEPOSIT);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type Invalid : Please Select Between withdraw and deppsit");
        }
        return buckets;
    }


    private ArrayList<TrxCardless> getTrxCardlessBasedOnOption(Account account, String redeemed, CardLessEnum type){
        ArrayList<TrxCardless> t;

        if(redeemed.equalsIgnoreCase("true") || redeemed.equalsIgnoreCase("false")){
            t = new ArrayList<>(trxCardlessRepo.getTrxByOption(account, Boolean.valueOf(redeemed), type));
        } else if (redeemed.equalsIgnoreCase("all")) {
            t = new ArrayList<>(trxCardlessRepo.getTrxByOption(account, type));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request : Only accept `true`, `false`, and `all` as a status");
        }

        return t;
    }
}
