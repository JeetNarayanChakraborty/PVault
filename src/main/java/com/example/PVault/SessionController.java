package com.example.PVault;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;



@RestController
public class SessionController 
{
    private final PersistentTokenBasedRememberMeServices rememberMeServices;
    
    public SessionController(PersistentTokenBasedRememberMeServices rememberMeServices) 
    {
        this.rememberMeServices = rememberMeServices;
    }
    
    @PostMapping("/invalidate-session")
    public ResponseEntity<String> invalidateSession(HttpServletRequest request, 
                                                   	HttpServletResponse response) 
    {
        try 
        {
            HttpSession session = request.getSession(false); // Get current session
            if (session != null) 
            {
                session.invalidate();
            }
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication(); // Clear remember-me token from database
            
            if(auth != null)  
            {
                rememberMeServices.logout(request, response, auth);
            }
                        
            Cookie rememberMeCookie = new Cookie("remember-me", null); // Manually clear remember-me cookie
            rememberMeCookie.setPath("/");
            rememberMeCookie.setMaxAge(0);
            response.addCookie(rememberMeCookie);
                        
            SecurityContextHolder.clearContext(); // Clear security context
                        
            Cookie cookie = new Cookie("JSESSIONID", null); // Clear JSESSIONID cookie
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            
            return ResponseEntity.ok("Session invalidated successfully");           
        } 
        
        catch(Exception e) 
        {
            return ResponseEntity.ok("Session cleared"); // Return OK even if there were issues
        }
    }
}










