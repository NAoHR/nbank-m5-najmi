package id.co.indivara.jdt12.najmi.nbank.service;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxCardless;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.OnlyMoneyDepositWithdrawRequest;

import java.util.HashMap;
import java.util.List;

public interface AppService {
    Customer showDetailCustomer(Customer c); // customer token
    List<Account> showCustomerAccount(Customer c); // customer token

    HashMap<String, Object> showAccountDetailAndTransaction(Account account, String type); // account token

    TrxTransferReferencedId transferWithApp(Account c, AtmAndAppTransferRequest t); // account token

    TrxCardless cardlessWithdraw(Account account, OnlyMoneyDepositWithdrawRequest money);
}
