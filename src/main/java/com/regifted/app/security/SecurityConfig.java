package com.regifted.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  
  private final CustomUserDetailsService userDetailsService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        
        // Configuration des autorisations
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
            
            // Pages publiques
            .requestMatchers("/", "/register", "/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/users").permitAll()
            .requestMatchers(HttpMethod.GET, "/users/{uuid}").permitAll()
            .requestMatchers(HttpMethod.GET, "/users/*/items").permitAll()
            
            // Tous les autres GET publics (items, etc.)
            .requestMatchers(HttpMethod.GET, "/items/**").permitAll()
            
            // Endpoints protégés nécessitant authentification
            .requestMatchers("/users/me/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/users/me/likes").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/users/me/likes").authenticated()
            .requestMatchers(HttpMethod.POST, "/items").authenticated()
            .requestMatchers(HttpMethod.PUT, "/items/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/items/**").authenticated()
            .requestMatchers("/items/*/messages").authenticated()
            
            // Tout le reste nécessite authentification
            .anyRequest().authenticated()
        )
        
        .httpBasic(basic -> basic
            .realmName("Regifted API")
            .authenticationEntryPoint(basicAuthenticationEntryPoint())
        )
        
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        
        .formLogin(form -> form.disable())
        .logout(logout -> logout
            .permitAll()
            .logoutSuccessUrl("/")
        )
    
        .userDetailsService(userDetailsService);

    return http.build();
  }

  /**
   * Entry point personnalisé pour HTTP Basic
   * Permet de renvoyer des erreurs JSON propres pour les API
   */
  @Bean
  public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
    BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
    entryPoint.setRealmName("Regifted API");
    return entryPoint;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}