package id.co.indivara.jdt12.najmi.nbank.service;

import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxTransfer;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.DepositRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.WithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterCustomerReq;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.TransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterAccountResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterCustomerResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public interface AdminService {


    RegisterCustomerResponse registerCustomer(RegisterCustomerReq customerReq);
    RegisterAccountResponse registerAccount(RegisterAccountRequest accountRequest);

    // display Mandatory
    HashMap<String, Object> displayAccountTransactionActivity(UUID accountId, String transactionType);
    DisplayCustomerAndAllAccountsResponse displayCustomerAndAllAccounts(UUID customerId);

    // transaction mandatory
    TrxDeposit depositToAnAccount(DepositRequest depo);
    TrxWithdraw withdrawFromAnAccount(WithdrawRequest wd);
    TrxTransferReferencedId transferFromAccountToAccount(TransferRequest transferRequest);
}
