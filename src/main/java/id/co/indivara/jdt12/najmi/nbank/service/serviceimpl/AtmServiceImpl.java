package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxCardless;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.enums.CardLessEnum;
import id.co.indivara.jdt12.najmi.nbank.exception.MinimumIs5KException;
import id.co.indivara.jdt12.najmi.nbank.exception.RedeemTicketNotFound;
import id.co.indivara.jdt12.najmi.nbank.model.RedeemWithdrawOrDepositRequest;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.OnlyMoneyDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.TrxCardlessRepo;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.AtmService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AtmServiceImpl implements AtmService {
    private final ValidatorService validatorService;
    private final AccountService accountService;
    private final TrxCardlessRepo trxCardlessRepo;
    private final AccountRepo accountRepo;

    @Override
    public HashMap<String, Object> displayAccountTransactionActivity(Account account, String type) {

        return accountService.displayAccountTransactionActivity(account, type);
    }

    @Override
    public TrxDeposit depositViaAtm(Account account, OnlyMoneyDepositWithdrawRequest depositRequest) {
        validatorService.validate(depositRequest);
        return accountService.deposit(account, depositRequest.getMoney(), true);
    }

    @Override
    public TrxWithdraw withdrawViaAtm(Account account, OnlyMoneyDepositWithdrawRequest withdrawRequest) {
        validatorService.validate(withdrawRequest);
        return accountService.withdraw(account, withdrawRequest.getMoney(), true);
    }

    @Override
    public TrxTransferReferencedId transferViaAtm(Account account, AtmAndAppTransferRequest transferRequest) {
        validatorService.validate(transferRequest);
        if(transferRequest.getAmount().compareTo(BigDecimal.valueOf(5_000)) < 0) throw new MinimumIs5KException();
        return accountService.transfer(account, transferRequest.getDestination(), transferRequest.getAmount(), false);
    }

    @Override
    public TrxDeposit redeemDeposit(RedeemWithdrawOrDepositRequest request) {
        validatorService.validate(request);

        TrxCardless trxCardless = trxCardlessRepo.findById(request.getUuid()).orElseThrow(RedeemTicketNotFound::new);
        if(trxCardless.getType().equals(CardLessEnum.WITHDRAW)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket Not Valid : This Is A Withdraw ticket, Please Redeem It On Withdraw Feature");
        if(trxCardless.getRedeemed().equals(Boolean.TRUE)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Ticket Redeemed : Ticket Is Already Redeemed at %s", trxCardless.getRedeemedTime().toString()));
        if(request.getMoney() == null || request.getMoney().compareTo(trxCardless.getAmount()) != 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Money Violation : Please Fulfil The Specified Amount Of money Requirements");

        trxCardless.setRedeemed(true);
        trxCardless.setRedeemedTime(Timestamp.valueOf(LocalDateTime.now()));
        trxCardlessRepo.save(trxCardless);

        return accountService.deposit(trxCardless.getAccount().getAccountId(), request.getMoney(), true);
    }

    @Override
    public TrxWithdraw redeemWithdraw(RedeemWithdrawOrDepositRequest request) {
        validatorService.validate(request);

        TrxCardless trxCardless = trxCardlessRepo.findById(request.getUuid()).orElseThrow(RedeemTicketNotFound::new);
        if(trxCardless.getType().equals(CardLessEnum.DEPOSIT)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket Not Valid : This Is A Deposit ticket, Please Redeem It On Deposit Feature");
        if(trxCardless.getRedeemed().equals(Boolean.TRUE)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Ticket Redeemed : Ticket Is Already Redeemed at %s", trxCardless.getRedeemedTime()));

        trxCardless.setRedeemed(true);
        trxCardless.setRedeemedTime(Timestamp.valueOf(LocalDateTime.now()));
        trxCardlessRepo.save(trxCardless);

        return accountService.withdraw(trxCardless.getAccount().getAccountId(), trxCardless.getAmount(), true);
    }
}
