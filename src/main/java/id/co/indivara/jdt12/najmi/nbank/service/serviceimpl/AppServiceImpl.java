package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;

import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;

import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class AppServiceImpl implements AppService {

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
}
