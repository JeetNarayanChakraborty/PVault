package security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.example.PVault.service.CustomUserDetailsService;



@Configuration
@EnableWebSecurity
public class SecurityConfig
{
	private final DataSource dataSource;
	private final CustomUserDetailsService customUserDetailsService;
    
	
	public SecurityConfig(DataSource dataSource, CustomUserDetailsService customUserDetailsService) 
	{
        this.dataSource = dataSource;
        this.customUserDetailsService = customUserDetailsService;
    }
	
    @Bean
    public PasswordEncoder passwordEncoder() 
    {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public PersistentTokenRepository persistentTokenRepository() 
    {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }
    
    @Bean
    public PersistentTokenBasedRememberMeServices rememberMeServices(
            UserDetailsService userDetailsService) 
    {

        PersistentTokenBasedRememberMeServices rememberMeServices =
            new PersistentTokenBasedRememberMeServices(
                "remember-me",
                userDetailsService,
                persistentTokenRepository()
            );

        rememberMeServices.setAlwaysRemember(false);
        return rememberMeServices;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   PersistentTokenBasedRememberMeServices rememberMeServices) throws Exception 
    {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                .invalidSessionUrl("/getHome?sessionExpired=true")
                .maximumSessions(1)
                .expiredUrl("/getHome?sessionExpired=true")
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .rememberMe(remember -> remember
                .rememberMeServices(rememberMeServices) // Injected bean
                .tokenValiditySeconds(2592000) // 30 days
                .rememberMeParameter("remember-me")
            );
        
        return http.build();
    }
}






