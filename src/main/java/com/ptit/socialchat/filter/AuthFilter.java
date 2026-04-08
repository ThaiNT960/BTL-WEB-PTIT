package com.ptit.socialchat.filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("userId") != null);

        if (!loggedIn) {
            res.sendRedirect(req.getContextPath() + "/LoginServlet");
        } else {
            String role = (String) session.getAttribute("role");
            String path = req.getServletPath();

            if ("/AdminServlet".equals(path) && !"ROLE_ADMIN".equals(role)) {
                res.sendRedirect(req.getContextPath() + "/HomeServlet");
                return;
            }

            if ("ROLE_ADMIN".equals(role)) {
                if ("/HomeServlet".equals(path) || "/FriendServlet".equals(path) || 
                    "/ChatServlet".equals(path) || "/ProfileServlet".equals(path) || 
                    "/SearchServlet".equals(path)) {
                    res.sendRedirect(req.getContextPath() + "/AdminServlet");
                    return;
                }
            }

            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
