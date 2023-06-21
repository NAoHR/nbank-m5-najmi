package id.co.indivara.jdt12.najmi.nbank.service.helper;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxTransfer;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.exception.*;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.repo.TrxDepositRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.TrxTransferRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.TrxWithdrawRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AccountHelper {

    @Autowired
    private TrxDepositRepo trxDepositRepo;

    @Autowired
    private TrxTransferRepo trxTransferRepo;

    @Autowired
    private TrxWithdrawRepo trxWithdrawRepo;


    public final void checkAccountType(Account account){
        if(account.getAccountType().equals(AccountTypeEnum.TIME_DEPOSIT)){
            LocalDateTime openTime = account.getOpenDate().toLocalDateTime();
            LocalDateTime monthOkToDoTransaction = openTime.plusMonths(account.getDepositMonth());

            if(LocalDateTime.now().isBefore(monthOkToDoTransaction)){
                throw new ItsTimeDepositAccountException("transaction", monthOkToDoTransaction.toString());
            }
        }
    }

    public final void checkMoneyWithBankOrAdmin(BigDecimal money){
        if(money.remainder(BigDecimal.valueOf(50_000)).compareTo(BigDecimal.ZERO) != 0){
            throw new MultipleOf50kException();
        }
    }

    public final void checkAllowedMinimumBalance(Account account, BigDecimal calculatedMoney){
        if(
                account.getAccountType().equals(AccountTypeEnum.CHECKING) && calculatedMoney.compareTo(BigDecimal.valueOf(10_000)) < 0
                ||
                account.getAccountType().equals(AccountTypeEnum.SAVINGS) && calculatedMoney.compareTo(BigDecimal.valueOf(50_000)) < 0){
            throw new InsufficientBalanceException();
        }
    }


    public final List<TrxTransferReferencedId> transferActivity(List<TrxTransfer> transfers){
        return transfers
                .stream()
                .map(e -> TrxTransferReferencedId.builder()
                        .account(e.getAccount().getAccountNumber())
                        .destination(e.getDestination().getAccountNumber())
                        .amount(e.getAmount())
                        .timestamp(e.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    public final void exceedingAmountOfTransaction(Account ac, TransactionTypeEnum t, BigDecimal currentTransaction){
        BigDecimal todayTransaction = getTodayTransaction(t, ac);
        if(todayTransaction == null)todayTransaction = BigDecimal.ZERO;

        BigDecimal money = currentTransaction.add(todayTransaction);
        BigDecimal limit = getLimitTransactionVaulue(ac, t);

        if(todayTransaction.compareTo(limit) < 0 && money.compareTo(limit) > 0){
            throw new TransactionOverflowFromExceedingException(t, limit.subtract(todayTransaction));
        } else if (money.compareTo(limit) > 0) {
            throw new ExceedingAmountTransaction(t);
        }
    }


    private BigDecimal getTodayTransaction(TransactionTypeEnum t, Account account){
        switch (t){
            case DEPOSIT:
                return trxDepositRepo.getTodayTransaction(account);
            case TRANSFER:
                return trxTransferRepo.getTodayTransaction(account);
            case WITHDRAW:
                return trxWithdrawRepo.getTodayTransaction(account);
            default:
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal : Internal Server Error");
        }
    }

    private BigDecimal getLimitTransactionVaulue(Account ac, TransactionTypeEnum t){
        if (Objects.requireNonNull(ac.getAccountType()) == AccountTypeEnum.CHECKING) {
            return limitTransaction(t, 15_000_000L, 20_000_000L, 20_000_000);
        }
        return limitTransaction(t, 35_000_000L, 15_000_000L, 10_000_000);
    }
    private BigDecimal limitTransaction(TransactionTypeEnum t, Long deposit, long transfer, long withdraw){
        switch (t){
            case DEPOSIT:
                return new BigDecimal(deposit);
            case TRANSFER:
                return new BigDecimal(transfer);
            case WITHDRAW:
                return new BigDecimal(withdraw);
            default:
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal : Internal Server Error");
        }
    }
}
