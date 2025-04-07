package security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;



public class SecurityConfigTest 
{
    @Mock
    private HttpSecurity httpSecurity;
    
    @InjectMocks
    private SecurityConfig securityConfig;
    
    private ArgumentCaptor<Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>> authorizeRequestsCaptor;
    private ArgumentCaptor<Customizer<SessionManagementConfigurer<HttpSecurity>>> sessionManagementCaptor;
    private ArgumentCaptor<Customizer<FormLoginConfigurer<HttpSecurity>>> formLoginCaptor;
    private ArgumentCaptor<Customizer<HttpBasicConfigurer<HttpSecurity>>> httpBasicCaptor;
    private ArgumentCaptor<Customizer<RememberMeConfigurer<HttpSecurity>>> rememberMeCaptor;
    private ArgumentCaptor<Customizer<CsrfConfigurer<HttpSecurity>>> csrfCaptor;
    
    @BeforeEach
    public void setup() throws Exception 
    {
        // Initialize the argument captors
        authorizeRequestsCaptor = ArgumentCaptor.forClass(Customizer.class);
        sessionManagementCaptor = ArgumentCaptor.forClass(Customizer.class);
        formLoginCaptor = ArgumentCaptor.forClass(Customizer.class);
        httpBasicCaptor = ArgumentCaptor.forClass(Customizer.class);
        rememberMeCaptor = ArgumentCaptor.forClass(Customizer.class);
        csrfCaptor = ArgumentCaptor.forClass(Customizer.class);
        
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        // Mock the builder pattern returns
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.rememberMe(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        
        // Mock the build method to return a mock filter chain
        DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(mockFilterChain);
    }
    
    @Test
    public void testSecurityFilterChain() throws Exception 
    {
        // When - call the actual method we're testing
        SecurityFilterChain result = securityConfig.securityFilterChain(httpSecurity);
        
        // Then
        assertNotNull(result);
        
        // Verify that each security configuration method was called with the correct customizer
        verify(httpSecurity).authorizeHttpRequests(authorizeRequestsCaptor.capture());
        verify(httpSecurity).sessionManagement(sessionManagementCaptor.capture());
        verify(httpSecurity).formLogin(formLoginCaptor.capture());
        verify(httpSecurity).httpBasic(httpBasicCaptor.capture());
        verify(httpSecurity).rememberMe(rememberMeCaptor.capture());
        verify(httpSecurity).csrf(csrfCaptor.capture());
        verify(httpSecurity).build();
    }
}