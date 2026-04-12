package com.ptit.socialchat.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebFilter("/*")
public class AntiCSRFFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            String csrfToken = (String) session.getAttribute("csrfToken");
            if (csrfToken == null) {
                csrfToken = UUID.randomUUID().toString();
                session.setAttribute("csrfToken", csrfToken);
            }

            String method = req.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                String reqCsrfToken = req.getHeader("X-CSRF-TOKEN");
                if (reqCsrfToken == null || reqCsrfToken.isEmpty()) {
                    reqCsrfToken = req.getParameter("csrfToken");
                }

                if (reqCsrfToken == null || !reqCsrfToken.equals(csrfToken)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"error\": \"Invalid or missing CSRF token\"}");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
