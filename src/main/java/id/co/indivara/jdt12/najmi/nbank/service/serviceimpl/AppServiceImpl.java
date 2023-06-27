package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;

import id.co.indivara.jdt12.najmi.nbank.entity.TrxCardless;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.OnlyMoneyDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;

import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.AppService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

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
        accountHelper.checkAccountStatus(account);
        accountHelper.checkAccountType(account);
        accountHelper.multipleOf50kValidator(request.getMoney());

        return TrxCardless.builder().build();
    }
}
