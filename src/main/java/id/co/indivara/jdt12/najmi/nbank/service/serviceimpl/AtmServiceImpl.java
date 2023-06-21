package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.AtmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public TrxTransferReferencedId transferViaAtm(Account account, AtmTransferRequest transferRequest) {
        validatorService.validate(transferRequest);
        return accountService.transfer(account, transferRequest.getDestination(), transferRequest.getAmount(), true);
    }
}
