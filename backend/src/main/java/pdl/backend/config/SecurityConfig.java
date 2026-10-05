package pdl.backend.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

   
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors() // <-- active la config CORS définie dans CorsConfig
            .and()
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/users/**").permitAll()  // login/register ouverts
                .requestMatchers("/images/**").permitAll() // images accessibles publiquement
                .anyRequest().permitAll()
            )
            .httpBasic().disable(); // HTTP Basic pour endpoints sécurisés

        return http.build();
    }
}