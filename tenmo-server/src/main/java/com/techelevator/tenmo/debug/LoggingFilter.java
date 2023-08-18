package com.techelevator.tenmo.debug;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class LoggingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization");

        // Here you can use your logger to print the token
        System.out.println("Authorization Header: " + authHeader);

        chain.doFilter(request, response);
    }

    // other methods...
}