<%@ page contentType="text/html; charset=UTF-8" %><% response.sendRedirect(request.getContextPath() + (session.getAttribute("userId") == null ? "/auth/login" : "/app/dashboard")); %>
