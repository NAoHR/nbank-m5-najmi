package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.exception.MinimumIs5KException;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.AtmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;

@Service
public class AtmServiceImpl implements AtmService {


    @Autowired
    private ValidatorService validatorService;
    @Autowired
    private AccountService accountService;

    @Override
    public HashMap<String, Object> displayAccountTransactionActivity(Account account, String type) {

        return accountService.displayAccountTransactionActivity(account, type);
    }

    @Override
    public TrxDeposit depositViaAtm(Account account, AtmDepositWithdrawRequest depositRequest) {
        validatorService.validate(depositRequest);
        return accountService.deposit(account, depositRequest.getMoney(), true);
    }

    @Override
    public TrxWithdraw withdrawViaAtm(Account account, AtmDepositWithdrawRequest withdrawRequest) {
        validatorService.validate(withdrawRequest);
        return accountService.withdraw(account, withdrawRequest.getMoney(), true);
    }

    @Override
    public TrxTransferReferencedId transferViaAtm(Account account, AtmAndAppTransferRequest transferRequest) {
        validatorService.validate(transferRequest);
        if(transferRequest.getAmount().compareTo(BigDecimal.valueOf(5_000)) < 0) throw new MinimumIs5KException();
        return accountService.transfer(account, transferRequest.getDestination(), transferRequest.getAmount(), false);
    }
}
