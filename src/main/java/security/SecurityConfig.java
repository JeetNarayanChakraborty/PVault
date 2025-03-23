package security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;



@Configuration
@EnableWebSecurity
public class SecurityConfig
{
	private final DataSource dataSource;
    
	
    public SecurityConfig(DataSource dataSource) 
    {
        this.dataSource = dataSource;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception 
    {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                    .invalidSessionUrl("/getHome?sessionExpired=true") // Redirect on session timeout
            )
            .formLogin(AbstractHttpConfigurer::disable)  
            .httpBasic(AbstractHttpConfigurer::disable)
            .rememberMe(remember -> remember
                    .tokenRepository(persistentTokenRepository())
                    .tokenValiditySeconds(2592000) // 30 day
                    .rememberMeParameter("remember-me") 
            );

        return http.build();
    }
}






