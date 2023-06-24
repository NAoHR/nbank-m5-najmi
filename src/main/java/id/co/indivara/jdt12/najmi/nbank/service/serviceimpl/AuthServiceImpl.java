package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.AccountAuth;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import id.co.indivara.jdt12.najmi.nbank.exception.AccountNotFoundException;
import id.co.indivara.jdt12.najmi.nbank.exception.CustomerNotFoundException;
import id.co.indivara.jdt12.najmi.nbank.model.request.account.AuthAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.customer.AuthCustomerRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.TokenResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerRepo;
import id.co.indivara.jdt12.najmi.nbank.security.JsonWebToken;
import id.co.indivara.jdt12.najmi.nbank.service.AuthService;
import id.co.indivara.jdt12.najmi.nbank.service.helper.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.type.NullType;
import javax.transaction.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private CustomerAuthRepo customerAuthRepo;


    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountAuthRepo accountAuthRepo;


    @Autowired
    private JsonWebToken jsonWebToken;

    @Autowired
    private AuthHelper authHelper;


    @Override
    @Transactional
    public TokenResponse customerLogin(AuthCustomerRequest r) {
        validatorService.validate(r);

        Customer customer = customerRepo.findByEmail(r.getEmail()).orElseThrow(CustomerNotFoundException::new);
        authHelper.validatePassword(r.getPassword(), customer.getPassword());

        String token = jsonWebToken.generateToken(customer);

        CustomerAuth customerAuth = customerAuthRepo.findByCustomer(customer);
        customerAuth.setToken(token);

        customerAuthRepo.save(customerAuth);

        return TokenResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public NullType customerLogout(Customer c) {
        CustomerAuth customerAuth = customerAuthRepo.findByCustomer(c);
        customerAuth.setToken(null);
        customerAuthRepo.save(customerAuth);
        return null;
    }

    @Override
    public TokenResponse accountLogin(AuthAccountRequest r) {
        validatorService.validate(r);

        Account account = accountRepo.findById(r.getAcid()).orElseThrow(AccountNotFoundException::new);
        authHelper.validatePassword(r.getPin(), account.getPassword());

        String token = jsonWebToken.generateToken(account);

        AccountAuth accountAuth = accountAuthRepo.findByAccount(account);
        accountAuth.setToken(token);

        accountAuthRepo.save(accountAuth);

        return TokenResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public NullType accountLogout(Account account) {
        AccountAuth accountAuth = accountAuthRepo.findByAccount(account);
        accountAuth.setToken(null);
        accountAuthRepo.save(accountAuth);
        return null;
    }
}
