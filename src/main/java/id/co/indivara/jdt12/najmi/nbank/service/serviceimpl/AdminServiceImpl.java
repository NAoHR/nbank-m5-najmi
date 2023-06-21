package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.*;

import id.co.indivara.jdt12.najmi.nbank.exception.*;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.DepositRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.WithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterCustomerReq;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.TransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterAccountResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterCustomerResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.*;
import id.co.indivara.jdt12.najmi.nbank.security.BCrypt;
import id.co.indivara.jdt12.najmi.nbank.service.AccountService;
import id.co.indivara.jdt12.najmi.nbank.service.AdminService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountCustomerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Service
public class AdminServiceImpl implements AdminService {
    // helper
    @Autowired
    private AccountCustomerHelper accountCustomerHelper;

    // service
    @Autowired
    private ValidatorService validatorService;

    // repo
    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerAuthRepo customerAuthRepo;

    @Autowired
    private AccountAuthRepo accountAuthRepo;

    @Override
    @Transactional
    public RegisterCustomerResponse registerCustomer(RegisterCustomerReq customerReq) {
        validatorService.validate(customerReq);
        if(customerRepo.existsByEmail(customerReq.getEmail())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot Create User : Email Already Been Used");
        }



        Customer customer = Customer.builder()
                .customerId(UUID.randomUUID())
                .identityNumber(customerReq.getIdentityNumber())
                .firstName(customerReq.getFirstName())
                .lastName(customerReq.getLastName())
                .phoneNumber(customerReq.getPhoneNumber())
                .email(customerReq.getEmail())
                .dob(customerReq.getDob())
                .address(customerReq.getAddress())
                .occupation(customerReq.getOccupation())
                .income(customerReq.getIncome())
                .maritalStatus(customerReq.getMaritalStatus())
                .highestEducation(customerReq.getHighestEducation())
                .password(BCrypt.hashpw(customerReq.getPassword(), BCrypt.gensalt()))
                .build();


        Customer savedCustomer = customerRepo.save(customer);

        customerAuthRepo.save(CustomerAuth.builder()
                        .customer(savedCustomer)
                        .token(null)
                .build());

        return RegisterCustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .identityNumber(customer.getIdentityNumber())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .dob(customer.getDob())
                .address(customer.getAddress())
                .occupation(customer.getOccupation())
                .income(customer.getIncome())
                .maritalStatus(customer.getMaritalStatus())
                .highestEducation(customer.getHighestEducation())
                .email(customer.getEmail())
                .build();
    }

    @Override
    public RegisterAccountResponse registerAccount(RegisterAccountRequest accountRequest) {

        validatorService.validate(accountRequest);
        Customer customer = customerRepo.findById(accountRequest.getCustomerId())
                .orElseThrow(CustomerNotFoundException::new);


        Account account = Account.builder()
                .accountId(UUID.randomUUID())
                .customer(customer)
                .accountNumber(accountCustomerHelper.generateAccountNumber())
                .password(BCrypt.hashpw(accountRequest.getAccountPassword(), BCrypt.gensalt()))
                .status(accountRequest.getStatus())
                .accountType(accountRequest.getAccountType())
                .balance(accountCustomerHelper.customValidateBalanceAndType(accountRequest.getBalance(), accountRequest.getAccountType()))
                .depositMonth(accountCustomerHelper.customValidateUserMonth(accountRequest.getAccountType(), accountRequest.getDepositMonth()))
                .openDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        Account savedAccount = accountRepo.save(account);
        accountAuthRepo.save(AccountAuth.builder()
                        .account(savedAccount)
                        .token(null)
                .build());

        return RegisterAccountResponse.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .status(account.getStatus())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .openDate(account.getOpenDate())
                .depositMonth(account.getDepositMonth())
                .build();
    }

    @Override
    public HashMap<String, Object> displayAccountTransactionActivity(String accountNumber, String transactionType) {
        return accountService.displayAccountTransactionActivity(accountNumber, transactionType);
    }

    @Override
    public DisplayCustomerAndAllAccountsResponse displayCustomerAndAllAccounts(String customerEmail) {
        return accountService.displayCustomerAndAllAccounts(customerEmail);
    }

    @Override
    @Transactional
    public TrxDeposit depositToAnAccount(DepositRequest depo) {
        validatorService.validate(depo);
        return accountService.deposit(depo.getUid(), depo.getMoney(), true);

    }

    @Override
    @Transactional
    public TrxWithdraw withdrawFromAnAccount(WithdrawRequest wd) {
        validatorService.validate(wd);
        return accountService.withdraw(wd.getUid(), wd.getMoney(), true);

    }

    @Override
    @Transactional
    public TrxTransferReferencedId transferFromAccountToAccount(TransferRequest transferRequest) {
        validatorService.validate(transferRequest);
        return accountService.transfer(transferRequest.getFrom(), transferRequest.getDestination(), transferRequest.getAmount(), true);
    }
}
