package id.co.indivara.jdt12.najmi.nbank.service;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmTransferRequest;


import java.util.HashMap;


public interface AtmService {
    HashMap<String, Object> displayAccountTransactionActivity(Account account, String type);
    TrxDeposit depositViaAtm(Account account, AtmDepositWithdrawRequest depositRequest);
    TrxWithdraw withdrawViaAtm(Account account, AtmDepositWithdrawRequest withdrawRequest);

    TrxTransferReferencedId transferViaAtm(Account account, AtmTransferRequest transferRequest);
}
