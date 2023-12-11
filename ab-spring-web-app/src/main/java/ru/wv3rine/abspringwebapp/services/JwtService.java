package ru.wv3rine.abspringwebapp.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Класс, отвечающий за взаимодействие с jwt
 */
@Service
public class JwtService {
    // прикол его здесь оставлять))
    @Value("${application.token.signing.key}")
    private String SECRET_KEY;
    @Value("${application.token.expiration}")
    private long jwtExpiration;

    /**
     * Взятие логина (subject) из jwt
     * @param token jwt (токен)
     * @return логин пользователя из токена
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Взятие любого поля (claim) из jwt
     * @param token jwt (токен)
     * @param claimsResolver функция, извлекающая поле (claim) из токена (часто
     *                       из класса {@link Claims})
     * @return извлекаемое поле
     * @param <T> тип извлекаемого поля
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Генерация jwt (токена) для пользователя
     * @param userDetails пользователь, для которого генерируется токен
     * @return jwt (токен)
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Генерация jwt (токена) для пользователя с учетом дополнительных полей (claims)
     * @param extraClaims дополнительные поля (claims)
     * @param userDetails пользователь, для которого генерируется токен
     * @return jwt (токен)
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // todo
        // добавить refresh token
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Проверка, является ли jwt (токен) валидным для пользователя (он ли
     * закодирован в заданном токене)
     * @param token jwt (токен)
     * @param userDetails пользователь, для которого проводится проверка, его ли jwt
     * @return true, если токен принадлежит пользователю, false иначе
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
