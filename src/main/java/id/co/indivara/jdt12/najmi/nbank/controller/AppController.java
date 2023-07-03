package id.co.indivara.jdt12.najmi.nbank.controller;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.OnlyMoneyDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.service.AppService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/app")
public class AppController {

    @Autowired
    private AppService appService;

    @GetMapping("/customer")
    public ResponseEntity<Object> showDetailCustomer(@RequestAttribute("customer") Customer customer){
        return ResponseEntity.status(HttpStatus.OK).body(
                WebResponse.builder()
                        .message("Successfully Fetched")
                        .data(appService.showDetailCustomer(customer))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/customer/accounts")
    public ResponseEntity<Object> showCustomerAccounts(@RequestAttribute("customer") Customer customer){
        return ResponseEntity.status(HttpStatus.OK).body(
                WebResponse.builder()
                        .message("Successfully Fetched")
                        .data(appService.showCustomerAccount(customer))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/customer/account/detail")
    public ResponseEntity<Object> accountDetailAndTransaction(
            @RequestParam(value = "type", required = false, defaultValue = "all") String type,
            @RequestAttribute("account") Account account
    ){

        return ResponseEntity.status(HttpStatus.OK).body(
                WebResponse.builder()
                        .message("Successfully Fetched")
                        .data(appService.showAccountDetailAndTransaction(account, type))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }


    @PostMapping("/transfer")
    public ResponseEntity<Object> transferViaApp(
            @RequestAttribute("account") Account account,
            @RequestBody AtmAndAppTransferRequest atmAndAppTransferRequest
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Transfer Success")
                        .timestamp(LocalDateTime.now())
                        .data(appService.transferWithApp(account, atmAndAppTransferRequest))
                        .build()
        );
    }

    @PostMapping("/cardless/withdraw")
    public ResponseEntity<Object> withdrawCardless(
            @RequestAttribute("account") Account account,
            @RequestBody OnlyMoneyDepositWithdrawRequest request
            ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Cardless Withdraw Success")
                        .data(appService.cardlessWithdraw(account, request))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/cardless/deposit")
    public ResponseEntity<Object> depositCardless(
            @RequestAttribute("account") Account account,
            @RequestBody OnlyMoneyDepositWithdrawRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Cardless Deposit Success")
                        .data(appService.cardlessDeposit(account, request))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/cardless/check/{type}")
    public ResponseEntity<Object> checkDepositWithdrawCardless(
            @RequestAttribute("account") Account account,
            @PathVariable("type") String type,
            @RequestParam(value = "redeemed", required = false, defaultValue = "true") String redeemed
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Checking Success")
                        .data(appService.checkOwnedCardlessTransaction(account, type, redeemed))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

}