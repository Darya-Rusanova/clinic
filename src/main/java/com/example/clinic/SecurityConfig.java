package com.example.clinic;

import com.example.clinic.service.ClientService;
import com.example.clinic.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private ClientService clientService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Публичные страницы (доступны всем)
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/api/**").permitAll()
                        .requestMatchers("/login", "/register").permitAll()

                        // Страница записи - доступна клиентам и админам
                        .requestMatchers("/booking/**").hasAnyRole("CLIENT", "ADMIN")

                        // Админ панель (только ADMIN)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Доктор панель (только DOCTOR)
                        .requestMatchers("/doctor/**").hasRole("DOCTOR")

                        // Клиент панель (только CLIENT)
                        .requestMatchers("/client/**").hasRole("CLIENT")

                        // Всё остальное требует авторизации
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/";

            for (var authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN")) {
                    redirectUrl = "/admin";
                    break;
                } else if (role.equals("ROLE_DOCTOR")) {
                    redirectUrl = "/doctor";
                    break;
                } else if (role.equals("ROLE_CLIENT")) {
                    String email = authentication.getName();
                    Integer clientId = clientService.getClientIdByEmail(email);
                    if (clientId != null) {
                        redirectUrl = "/client/" + clientId;
                    } else {
                        redirectUrl = "/";
                    }
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}