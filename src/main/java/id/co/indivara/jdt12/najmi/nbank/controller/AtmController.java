package id.co.indivara.jdt12.najmi.nbank.controller;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.model.RedeemWithdrawOrDepositRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.OnlyMoneyDepositWithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.AtmAndAppTransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.service.AtmService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/atm")
@SecurityRequirement(name = "bearerAccountAuth")
public class AtmController {

    @Autowired
    private AtmService atmService;
    @GetMapping("/transaction/activity")
    public final ResponseEntity<Object> accountActivity(
            @RequestParam(value = "type", required = false, defaultValue = "all") String type,
            @RequestAttribute("account") Account account
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                WebResponse.builder()
                        .message("Displayed")
                        .timestamp(LocalDateTime.now())
                        .data(atmService.displayAccountTransactionActivity(account, type))
                        .build()
        );
    }

    @PostMapping("/transaction/deposit")
    public final ResponseEntity<Object> depoAtm(
            @RequestAttribute("account") Account account,
            @RequestBody OnlyMoneyDepositWithdrawRequest depositRequest
            ){

        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Deposit Success")
                        .timestamp(LocalDateTime.now())
                        .data(atmService.depositViaAtm(account, depositRequest))
                        .build()
        );
    }

    @PostMapping("/transaction/withdraw")
    public final ResponseEntity<Object> witdrawAtm(
            @RequestAttribute("account") Account account,
            @RequestBody OnlyMoneyDepositWithdrawRequest withdrawRequest
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Deposit Success")
                        .timestamp(LocalDateTime.now())
                        .data(atmService.withdrawViaAtm(account, withdrawRequest))
                        .build()
        );
    }



    @PostMapping("/transaction/transfer")
    public final ResponseEntity<Object> transferAtm(
            @RequestAttribute("account") Account account,
            @RequestBody AtmAndAppTransferRequest atmTransferRequest
            ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Deposit Success")
                        .timestamp(LocalDateTime.now())
                        .data(atmService.transferViaAtm(account, atmTransferRequest))
                        .build()
        );
    }

    @PostMapping("/redeem/deposit")
    public final ResponseEntity<Object> redeemDeposit(
            @RequestBody RedeemWithdrawOrDepositRequest request
            ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Deposit Redeem Success")
                        .timestamp(LocalDateTime.now())
                        .data(atmService.redeemDeposit(request))
                        .build()
        );
    }

    @PostMapping("/redeem/withdraw")
    public final ResponseEntity<Object> redeemWithdraw(
            @RequestBody RedeemWithdrawOrDepositRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.builder()
                        .message("Withdraw Redeem Success")
                        .timestamp(LocalDateTime.now())
                        .data(atmService.redeemWithdraw(request))
                        .build()
        );
    }
}
