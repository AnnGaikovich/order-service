package org.example.orderservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
public class JwtTokenUtil {

    private final PublicKey publicKey;

    public JwtTokenUtil(@Value("${app.jwt.public-key}") Resource publicKeyResource) {
        try {
            this.publicKey = loadPublicKey(publicKeyResource);
            log.info("Public key loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load public key", e);
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            String publicKeyPem = new String(inputStream.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] encoded = Base64.getDecoder().decode(publicKeyPem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        }
    }

    public Long extractUserIdFromToken(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();

            Object userIdClaim = claims.get("userId");
            if (userIdClaim instanceof Integer) {
                return ((Integer) userIdClaim).longValue();
            } else if (userIdClaim instanceof Long) {
                return (Long) userIdClaim;
            } else if (userIdClaim instanceof String) {
                return Long.parseLong((String) userIdClaim);
            } else {
                throw new RuntimeException("Invalid userId format in token");
            }
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired token: " + e.getMessage());
        }
    }

    public List<String> extractRolesFromToken(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            if (roles == null) {
                log.warn("No roles found in token");
                return List.of();
            }

            log.debug("Extracted roles from token: {}", roles);
            return roles;
        } catch (Exception e) {
            log.error("Error extracting roles from token: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired token: " + e.getMessage());
        }
    }
}