package id.co.indivara.jdt12.najmi.nbank.service;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.model.request.account.AuthAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.customer.AuthCustomerRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.TokenResponse;

import javax.lang.model.type.NullType;

public interface AuthService {
    TokenResponse customerLogin(AuthCustomerRequest r);
    NullType customerLogout(Customer c);

    TokenResponse accountLogin(AuthAccountRequest r);
    NullType accountLogout(Account account);
}
