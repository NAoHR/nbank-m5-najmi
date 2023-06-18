package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import id.co.indivara.jdt12.najmi.nbank.repo.AccountAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerRepo;
import id.co.indivara.jdt12.najmi.nbank.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthServiceImpl implements AuthService {

    @Autowired
    ValidatorService validatorService;

    @Autowired
    CustomerRepo customerRepo;

    @Autowired
    CustomerAuthRepo customerAuthRepo;


    @Autowired
    AccountRepo accountRepo;

    @Autowired
    AccountAuthRepo accountAuthRepo;

    @Override
    public void customerLogin() {

    }

    @Override
    public void customerLogout() {

    }

    @Override
    public void accountLogin() {

    }

    @Override
    public void accountLogout() {

    }
}
