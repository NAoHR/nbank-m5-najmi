package id.co.indivara.jdt12.najmi.nbank.security;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.entity.Customer;

import id.co.indivara.jdt12.najmi.nbank.enums.AccountOrCustomerEnum;
import id.co.indivara.jdt12.najmi.nbank.exception.NotAuthorizedException;
import id.co.indivara.jdt12.najmi.nbank.exception.SessionExpiredException;
import id.co.indivara.jdt12.najmi.nbank.model.response.OccuredError;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountAuthRepo;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerAuthRepo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import javax.lang.model.type.NullType;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Component
public class JsonWebToken {

    @Autowired
    private CustomerAuthRepo customerAuthRepo;

    @Autowired
    private AccountAuthRepo accountAuthRepo;

    private final Key secretKey = Keys.hmacShaKeyFor("MJ4IafykM2RAhV2a7c5DUyMwKgrirm4z".getBytes());
    private final String issuer = "nbank.com";

    public final String generateToken(Account account){
        return Jwts.builder()
                .setSubject(account.getAccountId().toString())
                .setIssuer(issuer)
                .setExpiration(new Date(System.currentTimeMillis() + 15*60*1000))
                .signWith(secretKey)
                .compact();
    }

    public final String generateToken(Customer customer){
        return Jwts.builder()
                .setSubject(customer.getCustomerId().toString())
                .setIssuer(issuer)
                .setExpiration(new Date(System.currentTimeMillis() + 30*60*1000))
                .signWith(secretKey)
                .compact();
    }


    public final UUID parseToken(String token, AccountOrCustomerEnum type){
        if(token == null || !token.startsWith("Bearer ")){
            throw new NotAuthorizedException();
        }

        token = token.replace("Bearer ", "");

        try{
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build().parseClaimsJws(token);

            Claims body = claimsJws.getBody();

            boolean isExist = false;

            switch (type){
                case ACCOUNT:
                    isExist = accountAuthRepo.existsByToken(token);
                    break;
                case CUSTOMER:
                    isExist = customerAuthRepo.existsByToken(token);
                    break;
            }

            if(!isExist){
                throw new SessionExpiredException();
            }


            compareExpiration(body.getExpiration());
            return UUID.fromString(body.getSubject());

        }catch (JwtException jes){
            throw new SessionExpiredException();
        }
    }
    private void compareExpiration(Date d){
        if(d.before(new Date())){
            throw new SessionExpiredException();
        }
    }


    public void notAuthorizedException(HttpServletRequest response) throws Exception{
        WebResponse<NullType, String> r = WebResponse.<NullType, String>builder()
                .error(OccuredError.<String>builder()
                        .name("UNAUTHORIZED")
                        .detail("Please Login First")
                        .build())
                .data(null)
                .timestamp(LocalDateTime.now())
                .message("Failed Access")
                .build();


    }
}
