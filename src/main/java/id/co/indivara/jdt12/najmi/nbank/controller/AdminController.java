package id.co.indivara.jdt12.najmi.nbank.controller;

import id.co.indivara.jdt12.najmi.nbank.entity.TrxDeposit;
import id.co.indivara.jdt12.najmi.nbank.entity.TrxWithdraw;
import id.co.indivara.jdt12.najmi.nbank.model.TrxTransferReferencedId;
import id.co.indivara.jdt12.najmi.nbank.model.request.DepositRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.WithdrawRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterAccountRequest;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.RegisterCustomerReq;
import id.co.indivara.jdt12.najmi.nbank.model.request.admin.TransferRequest;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.DisplayCustomerAndAllAccountsResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterAccountResponse;
import id.co.indivara.jdt12.najmi.nbank.model.response.admin.RegisterCustomerResponse;
import id.co.indivara.jdt12.najmi.nbank.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/registercustomer")
    public ResponseEntity<Object> registerCustomer(@RequestBody RegisterCustomerReq customerReq){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.<RegisterCustomerResponse, NullType>builder()
                        .message("customer successfuly registered")
                        .data(adminService.registerCustomer(customerReq))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/registeraccount")
    public ResponseEntity<Object> registerAccount(@RequestBody RegisterAccountRequest accountRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.<RegisterAccountResponse, NullType>builder()
                        .message("account successfuly registered")
                        .data(adminService.registerAccount(accountRequest))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // detail
    @GetMapping("/customer/{email}")
    public ResponseEntity<Object> displayCustomerAndItsAccounts(@PathVariable("email") String customerEmail){
        return ResponseEntity.status(HttpStatus.FOUND).body(
                WebResponse.<DisplayCustomerAndAllAccountsResponse, NullType>builder()
                        .message("Customer Found")
                        .data(adminService.displayCustomerAndAllAccounts(customerEmail))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/customer/{acid}/transaction")
    public ResponseEntity<Object> displayTransactionActicity(
            @PathVariable String acid
            , @RequestParam(value = "type", required = false, defaultValue = "all") String type
            ){
        return ResponseEntity.status(HttpStatus.FOUND).body(
                WebResponse.<HashMap<String, Object>, NullType>builder()
                        .message("Customer Found")
                        .data(adminService.displayAccountTransactionActivity(acid, type))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }



    // transaction
    @PostMapping("/transaction/deposit")
    public ResponseEntity<Object> depoViaAdmin(@RequestBody DepositRequest depo){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.<TrxDeposit, NullType>builder()
                        .message("Deposit Success")
                        .data(adminService.depositToAnAccount(depo))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/transaction/withdraw")
    public ResponseEntity<Object> wdViaAdmin(@RequestBody WithdrawRequest wd){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.< TrxWithdraw, NullType >builder()
                        .message("Withdraw Success")
                        .data(adminService.withdrawFromAnAccount(wd))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/transaction/transfer")
    public ResponseEntity<Object> trViadAdmin(@RequestBody TransferRequest transferRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.<TrxTransferReferencedId, NullType >builder()
                        .message("Withdraw Success")
                        .data(adminService.transferFromAccountToAccount(transferRequest))
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

}
