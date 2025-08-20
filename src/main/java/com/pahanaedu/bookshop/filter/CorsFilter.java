package com.pahanaedu.bookshop.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * CORS (Cross-Origin Resource Sharing) filter
 */
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get the origin from the request
        String origin = httpRequest.getHeader("Origin");

        // Debug logging
        System.out.println("CORS Filter - Origin: " + origin);
        System.out.println("CORS Filter - Method: " + httpRequest.getMethod());

        // Always set specific origin for localhost development
        if (origin != null && (origin.equals("http://localhost:3000") ||
                              origin.equals("http://127.0.0.1:3000") ||
                              origin.startsWith("http://localhost:") ||
                              origin.startsWith("http://127.0.0.1:"))) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            System.out.println("CORS Filter - Setting origin to: " + origin);
        } else {
            // Default to frontend origin
            httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            System.out.println("CORS Filter - Setting default origin: http://localhost:3000");
        }

        // Set other CORS headers
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers",
            "Content-Type, Authorization, X-Requested-With, Accept, Origin");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // Handle preflight requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Continue with the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}
