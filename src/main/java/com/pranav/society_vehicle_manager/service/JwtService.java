package com.pranav.society_vehicle_manager.service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import com.pranav.society_vehicle_manager.security.DwaarPalPrincipal;
import org.springframework.stereotype.Service;
@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    public static final long ACCESS_TOKEN_TTL_SECONDS = 900;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(Authentication authentication) {
        DwaarPalPrincipal principal =
                (DwaarPalPrincipal) authentication.getPrincipal();

        Instant now = Instant.now();

        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("dwaar-pal")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS))
                .subject((principal.id().toString()))
                .claim("authorities", authorities)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                        JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
