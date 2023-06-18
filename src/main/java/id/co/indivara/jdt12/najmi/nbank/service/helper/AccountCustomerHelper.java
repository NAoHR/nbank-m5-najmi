package id.co.indivara.jdt12.najmi.nbank.service.helper;

import id.co.indivara.jdt12.najmi.nbank.enums.AccountTypeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

@Component
public class AccountCustomerHelper {
    public Integer customValidateUserMonth(AccountTypeEnum type, Integer month){
        if(type.compareTo(AccountTypeEnum.TIME_DEPOSIT) == 0 && month == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Deposit not valid : Please Fulfil specified deposit requirement");
        }

        return type.compareTo(AccountTypeEnum.TIME_DEPOSIT) == 0 ? month : 0;
    }

    public BigDecimal customValidateBalanceAndType(BigDecimal balance, AccountTypeEnum type){
        if((type.equals(AccountTypeEnum.SAVINGS) && balance.compareTo(BigDecimal.valueOf(100_000)) < 0) || (type.equals(AccountTypeEnum.TIME_DEPOSIT) && balance.compareTo(BigDecimal.valueOf(1_000_000)) < 0)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Money not valid : Please Fulfil specified Minimum money requirement");
        }
        return balance;
    }


    public String generateAccountNumber(){
        LocalDate dt = LocalDate.now();
        LocalTime tm = LocalTime.now();

        String month = (String.valueOf(dt.getMonthValue()).length() < 2 ? "0" : "")  + dt.getMonthValue();
        String day = (String.valueOf(dt.getDayOfMonth()).length() < 2 ? "0"  : "")  + dt.getDayOfMonth();

        String hour = (String.valueOf(tm.getHour()).length() < 2 ? "0"  : "") + tm.getHour();
        String minute = (String.valueOf(tm.getMinute()).length() < 2 ? "0"  : "") + tm.getMinute();
        String second = (String.valueOf(tm.getSecond()).length() < 2 ? "0"  : "") + tm.getSecond();

        return month+day+hour+minute+second + (new Random().nextInt(90000) + 10000);
    }
}
