package id.co.indivara.jdt12.najmi.nbank.service;

import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public interface AccountService {
    HashMap<String, Object> displayAccountTransactionActivity(UUID accountId, String transactionType);
    DisplayCustomerAndAllAccountsResponse displayCustomerAndAllAccounts(UUID customerId);
    TrxDeposit deposit(UUID accountId, BigDecimal amount, boolean isWithBankOrAdmin);
    TrxWithdraw withdraw(UUID accountId, BigDecimal amount, boolean isBankOrAdmin);
    TrxTransferReferencedId transfer(UUID from, UUID to, BigDecimal money, boolean isBankOrAdmin);
}
