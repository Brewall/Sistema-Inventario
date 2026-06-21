package com.edu.sistema_inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private JwtTokenProvider jwtTokenProvider;
    @Value("${app.security.dev-tools-enabled:false}")
    private boolean devToolsEnabled;

    public SecurityConfig() {
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> {
                authz.requestMatchers("/auth/**").permitAll();
                if (devToolsEnabled) {
                    authz.requestMatchers("/h2-console/**").permitAll();
                    authz.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs", "/swagger-resources/**", "/webjars/**").permitAll();
                }
                authz.requestMatchers(HttpMethod.GET, "/api/categorias").permitAll();
                authz.requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll();
                authz.requestMatchers(HttpMethod.GET, "/api/productos").permitAll();
                authz.requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll();
                authz.requestMatchers("/api/**").authenticated();
                authz.anyRequest().authenticated();
            })
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            ;

        if (this.jwtTokenProvider != null) {
            http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );
        }
        
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            if (devToolsEnabled) {
                web.ignoring()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**");
            }
        };
    }
}
