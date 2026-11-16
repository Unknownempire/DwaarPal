package com.pranav.society_vehicle_manager.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${app.jwt.secret}")
    private String jwtSecret;


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/users").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/societies",
                                "/api/societies/*/buildings",
                                "/api/buildings/*/flats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/pending").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/vehicles/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/vehicles").hasRole("RESIDENT")
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/*/entry-exit").hasRole("GUARD")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/my-vehicles").hasRole("RESIDENT")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/*").hasAnyRole("RESIDENT", "GUARD", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/societies/*/gates").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/memberships").hasRole("RESIDENT")
                        .requestMatchers(HttpMethod.GET, "/api/memberships/mine").hasRole("RESIDENT")
                        .requestMatchers(HttpMethod.GET, "/api/memberships/pending").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/memberships/*/decision").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/memberships/*/leave").hasRole("RESIDENT")
                        .requestMatchers(HttpMethod.GET, "/api/gates").hasRole("GUARD")
                        .requestMatchers(HttpMethod.GET, "/api/entry-exit/history").hasAnyRole("ADMIN", "GUARD")
                        .requestMatchers(HttpMethod.GET, "/api/entry-exit/vehicles/*/history")
                        .hasAnyRole("RESIDENT", "ADMIN", "GUARD")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("authorities");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return authenticationConverter;
    }
}
