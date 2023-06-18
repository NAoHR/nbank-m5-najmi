package id.co.indivara.jdt12.najmi.nbank.service.helper;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxTransfer;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.exception.InsufficientBalanceException;
import id.co.indivara.jdt12.najmi.nbank.exception.ItsTimeDepositAccountException;
import id.co.indivara.jdt12.najmi.nbank.exception.MultipleOf50kException;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountHelper {
    public final void checkAccountType(Account account){
        if(account.getAccountType().equals(AccountTypeEnum.TIME_DEPOSIT)){
            LocalDateTime openTime = account.getOpenDate().toLocalDateTime();
            LocalDateTime monthOkToDoTransaction = openTime.plusMonths(account.getDepositMonth());

            if(LocalDateTime.now().isBefore(monthOkToDoTransaction)){
                throw new ItsTimeDepositAccountException("deposit", monthOkToDoTransaction.toString());
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
}
