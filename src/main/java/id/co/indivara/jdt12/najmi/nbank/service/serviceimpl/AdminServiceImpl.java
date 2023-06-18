package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.*;

import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
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
import id.co.indivara.jdt12.najmi.nbank.service.AdminService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AccountCustomerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    // helper
    @Autowired
    AccountCustomerHelper accountCustomerHelper;

    // service
    @Autowired
    ValidatorService validatorService;

    // repo
    @Autowired
    CustomerRepo customerRepo;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    TrxDepositRepo trxDepositRepo;

    @Autowired
    TrxWithdrawRepo trxWithdrawRepo;

    @Autowired
    TrxTransferRepo trxTransferRepo;

    @Override
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


        customerRepo.save(customer);

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
        /*
         * Minimum balance rules
         * Savings -> 100_000
         * Checking -> 0
         * Time deposit -> 1_000_000
         */

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

        accountRepo.save(account);

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
    public HashMap<String, Object> displayAccountTransactionActivity(UUID accountId, String transactionType) {
        Account account = accountRepo.findById(accountId).orElseThrow(AccountNotFoundException::new);
        HashMap<String, Object> maps = new HashMap<>();
        switch (transactionType.toLowerCase()){
            case "transfer":
                maps.put("transfer", trxTransferRepo.findAllByAccount(account)
                        .stream()
                        .map(e -> TrxTransferReferencedId.builder()
                                .account(e.getAccount().getAccountNumber())
                                .destination(e.getDestination().getAccountNumber())
                                .amount(e.getAmount())
                                .timestamp(e.getTimestamp())
                                .build())
                        .collect(Collectors.toList()));
                break;
            case "deposit":
                maps.put("deposit", trxDepositRepo.findAllByAccount(account));
                break;
            case "withdraw":
                maps.put("withdraw", trxWithdrawRepo.findAllByAccount(account));
                break;
            case "all":
                maps.put("deposit", trxDepositRepo.findAllByAccount(account));
                maps.put("withdraw", trxWithdrawRepo.findAllByAccount(account));
                maps.put("transfer", trxTransferRepo.findAllByAccount(account));
                break;
            default:
                throw new ValidActivityException();
        }

        return maps;

    }

    @Override
    public DisplayCustomerAndAllAccountsResponse displayCustomerAndAllAccounts(UUID customerId) {
        Customer customer = customerRepo.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        List<Account> accounts = accountRepo.findAllByCustomer(customer);

        return DisplayCustomerAndAllAccountsResponse.builder()
                .customer(customer)
                .accounts(accounts)
                .build();
    }

    @Override
    @Transactional
    public TrxDeposit depositToAnAccount(DepositRequest depo) {

        validatorService.validate(depo);

        Account account = accountRepo.findById(depo.getUid())
                .orElseThrow(AccountNotFoundException::new);

        if(account.getAccountType().equals(AccountTypeEnum.TIME_DEPOSIT)){
            throw new ItsTimeDepositAccountException("deposit");
        }
        // validate money
        if(depo.getMoney().compareTo(BigDecimal.valueOf(10_000)) < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Money Not Valid : money must be bigger than 10.000");
        }

        account.setBalance(account.getBalance().add(depo.getMoney()));

        TrxDeposit deposit = TrxDeposit.builder()
                .account(account)
                .amount(depo.getMoney())
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        trxDepositRepo.save(deposit);
        accountRepo.save(account);

        return deposit;

    }

    @Override
    @Transactional
    public TrxWithdraw withdrawFromAnAccount(WithdrawRequest wd) {
        validatorService.validate(wd);

        Account account = accountRepo.findById(wd.getUid())
                .orElseThrow(AccountNotFoundException::new);

        // cek apakah akun itu time deposit
        if(account.getAccountType().equals(AccountTypeEnum.TIME_DEPOSIT)){
            throw new ItsTimeDepositAccountException("withdraw");
        }
        // cek apakah uang valid
        if(wd.getMoney().compareTo(BigDecimal.valueOf(10_000)) < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Money Not Valid : money must be bigger than 10.000");
        }

        BigDecimal nowMoney = account.getBalance().subtract(wd.getMoney());

        if(nowMoney.compareTo(BigDecimal.valueOf(0)) < 0){
            throw new InsufficientBalanceException();
        }

        // validate minimum saldo yang harus ada di rekening
        if(nowMoney.compareTo(BigDecimal.valueOf(10_000)) < 0){
            throw new MinimumBalanceException("Withdraw", BigDecimal.valueOf(10_000));
        }

        account.setBalance(nowMoney);

        TrxWithdraw withdraw = TrxWithdraw.builder()
                .account(account)
                .amount(wd.getMoney())
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        trxWithdrawRepo.save(withdraw);
        accountRepo.save(account);


        return withdraw;

    }

    @Override
    @Transactional
    public TrxTransferReferencedId transferFromAccountToAccount(TransferRequest transferRequest) {
        validatorService.validate(transferRequest);
        Account userFrom = accountRepo.findById(transferRequest.getFrom()).orElseThrow(AccountNotFoundException::new);
        Account userTo = accountRepo.findById(transferRequest.getDestination()).orElseThrow(AccountNotFoundException::new);

        if(userFrom.getAccountType().equals(AccountTypeEnum.TIME_DEPOSIT)){
            throw new ItsTimeDepositAccountException("withdraw");
        }
        if(userTo.getAccountType().equals(AccountTypeEnum.TIME_DEPOSIT)){
            throw new ItsTimeDepositAccountException("withdraw");
        }
        // cek apakah uang valid
        if(transferRequest.getAmount().compareTo(BigDecimal.valueOf(10_000)) < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Money Not Valid : money must be bigger than 10.000");
        }

        BigDecimal userFromMoney = userFrom.getBalance().subtract(transferRequest.getAmount());

        if(userFromMoney.compareTo(BigDecimal.valueOf(0)) < 0){
            throw new InsufficientBalanceException();
        }

        // validate minimum saldo yang harus ada di rekening
        if(userFromMoney.compareTo(BigDecimal.valueOf(10_000)) < 0){
            throw new MinimumBalanceException("Withdraw", BigDecimal.valueOf(10_000));
        }


        BigDecimal userToMoney = userTo.getBalance().add(transferRequest.getAmount());
        userFrom.setBalance(userFromMoney);
        userTo.setBalance(userToMoney);

        TrxTransfer trxTransfer = TrxTransfer.builder()
                .account(userFrom)
                .amount(transferRequest.getAmount())
                .destination(userTo)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        accountRepo.save(userTo);
        accountRepo.save(userFrom);
        trxTransferRepo.save(trxTransfer);

        return TrxTransferReferencedId.builder()
                .account(trxTransfer.getAccount().getAccountNumber())
                .transferId(trxTransfer.getTransferId())
                .amount(trxTransfer.getAmount())
                .destination(trxTransfer.getDestination().getAccountNumber())
                .timestamp(trxTransfer.getTimestamp())
                .build();
    }
}
