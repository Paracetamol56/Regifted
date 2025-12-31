package com.regifted.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              // Ressources statiques et publiques
              .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
              .requestMatchers("/", "/register").permitAll()
              .requestMatchers(org.springframework.http.HttpMethod.POST, "/users").permitAll()
              .requestMatchers(org.springframework.http.HttpMethod.GET, "/**").permitAll()
              
              // Ressources protégées
              .requestMatchers("/items/*/messages").authenticated()
              .anyRequest().authenticated())

          .httpBasic(org.springframework.security.config.Customizer.withDefaults())
          .formLogin(form -> form.disable())

          .logout(logout -> logout.permitAll())
          .userDetailsService(userDetailsService);

      return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
