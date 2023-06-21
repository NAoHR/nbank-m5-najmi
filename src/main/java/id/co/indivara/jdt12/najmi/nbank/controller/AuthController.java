package id.co.indivara.jdt12.najmi.nbank.controller;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.entity.CustomerAuth;
import id.co.indivara.jdt12.najmi.nbank.model.request.account.AuthAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.customer.AuthCustomerRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.TokenResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @PostMapping("/customer/login")
    public final ResponseEntity<Object> customerLogin(@RequestBody AuthCustomerRequest customerAuth){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.< TokenResponse, NullType >builder()
                        .message("Login Success")
                        .timestamp(LocalDateTime.now())
                        .data(authService.customerLogin(customerAuth))
                        .error(null)
                        .build()
        );
    }

    @DeleteMapping("/customer/logout")
    public final ResponseEntity<Object> customerLogout(@RequestAttribute("customer") Customer customer){
        return ResponseEntity.status(HttpStatus.OK).body(WebResponse.builder()
                        .message("Logged Out")
                        .data(authService.customerLogout(customer))
                        .error(null)
                        .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/account/login")
    public final ResponseEntity<Object> accountLogin(@RequestBody AuthAccountRequest accountAuth){
        return ResponseEntity.status(HttpStatus.OK).body(
                WebResponse.<TokenResponse, NullType>builder()
                        .message("Login Success")
                        .timestamp(LocalDateTime.now())
                        .data(authService.accountLogin(accountAuth))
                        .error(null)
                        .build()
        );
    }

    @DeleteMapping("/account/logout")
    public final ResponseEntity<Object> accountLogout(@RequestAttribute("account") Account account){
        return ResponseEntity.status(HttpStatus.OK).body(
                WebResponse.builder()
                        .error(null)
                        .data(authService.accountLogout(account))
                        .timestamp(LocalDateTime.now())
                        .message("Logged Out")
                        .build()
        );
    }
}
