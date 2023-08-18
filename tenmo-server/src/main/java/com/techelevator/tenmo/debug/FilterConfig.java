package com.techelevator.tenmo.debug;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter(){
        FilterRegistrationBean<LoggingFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new LoggingFilter());

        return registrationBean;
    }
}