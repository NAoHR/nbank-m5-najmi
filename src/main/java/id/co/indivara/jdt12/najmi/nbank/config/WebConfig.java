package id.co.indivara.jdt12.najmi.nbank.config;

import id.co.indivara.jdt12.najmi.nbank.security.AccountSecurityInterceptor;
import id.co.indivara.jdt12.najmi.nbank.security.CustomerSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    CustomerSecurityInterceptor customerSecurityInterceptor;

    @Autowired
    AccountSecurityInterceptor accountSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customerSecurityInterceptor)
                .addPathPatterns("/api/auth/customer/logout")
                .addPathPatterns("/api/app/**")
                .excludePathPatterns("/api/app/transfer")
                .excludePathPatterns("/api/app/customer/account/detail")
                .excludePathPatterns("/api/app/cardless/**")
                .excludePathPatterns("/api/atm/redeem/**")
        ;

        registry.addInterceptor(accountSecurityInterceptor)
                .addPathPatterns("/api/auth/account/logout")
                .addPathPatterns("/api/atm/**")
                .addPathPatterns("/api/app/transfer")
                .addPathPatterns("/api/app/customer/account/detail")
                .addPathPatterns("/api/app/cardless/**")
                .excludePathPatterns("/api/atm/redeem/**")
        ;
    }
}
