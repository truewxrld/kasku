package id.kasku;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityFilter implements Filter {
    public void doFilter(ServletRequest rawRequest,ServletResponse rawResponse,FilterChain chain) throws IOException,ServletException {
        HttpServletRequest req=(HttpServletRequest)rawRequest;
        HttpServletResponse res=(HttpServletResponse)rawResponse;
        req.setCharacterEncoding("UTF-8"); res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options","nosniff"); res.setHeader("X-Frame-Options","DENY");
        res.setHeader("Referrer-Policy","same-origin");
        res.setHeader("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'");
        String path=req.getRequestURI().substring(req.getContextPath().length());
        if (path.startsWith("/assets/") || path.equals("/health")) {chain.doFilter(req,res);return;}
        res.setHeader("Cache-Control","no-store");
        if (req.getContentLengthLong()>32768) {res.sendError(413); return;}
        HttpSession session=req.getSession();
        if (session.getAttribute("csrf")==null) {byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);session.setAttribute("csrf",Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));}
        if ("POST".equals(req.getMethod()) && !session.getAttribute("csrf").equals(req.getParameter("csrf"))) {res.sendError(403,"Sesi form tidak valid. Muat ulang halaman.");return;}
        if (path.startsWith("/app/") && session.getAttribute("userId")==null) {res.sendRedirect(req.getContextPath()+"/auth/login");return;}
        chain.doFilter(req,res);
    }
}
