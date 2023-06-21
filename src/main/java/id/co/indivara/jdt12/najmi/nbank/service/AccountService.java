package id.co.indivara.jdt12.najmi.nbank.service;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public interface AccountService {
    HashMap<String, Object> displayAccountTransactionActivity(String accountNumber, String transactionType);
    HashMap<String, Object> displayAccountTransactionActivity(Account account, String transactionType);

    DisplayCustomerAndAllAccountsResponse displayCustomerAndAllAccounts(String customerEmail);
    TrxDeposit deposit(UUID accountId, BigDecimal amount, boolean isWithBankOrAdmin);
    TrxDeposit deposit(Account account, BigDecimal decimal, boolean isWithBankOrAdmin);

    TrxWithdraw withdraw(UUID accountId, BigDecimal amount, boolean isBankOrAdmin);
    TrxWithdraw withdraw(Account account, BigDecimal amount, boolean isBankOrAdmin);

    TrxTransferReferencedId transfer(String from, String to, BigDecimal money, boolean isBankOrAdmin);
    TrxTransferReferencedId transfer(Account account, String to, BigDecimal money, boolean isBankOrAdmin);
}
