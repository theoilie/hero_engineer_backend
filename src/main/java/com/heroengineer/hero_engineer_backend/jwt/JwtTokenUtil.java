package com.heroengineer.hero_engineer_backend.jwt;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.heroengineer.hero_engineer_backend.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.gson.io.GsonSerializer;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.impl.DefaultClock;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

@Component
public class JwtTokenUtil implements Serializable {

  private static final long serialVersionUID = -3301605591108950415L;

  private Clock clock = DefaultClock.INSTANCE;

  private final SecretKey secretKey;

  @Value("${jwt.token.expiration.in.seconds}")
  private Long expiration;
  @Value("${jwt.http.request.header}")
  private String tokenHeader;

  @Autowired
  public JwtTokenUtil(SecretKey secretKey) {
    this.secretKey = secretKey;
  }

  public String getUsernameFromRequest(HttpServletRequest request)
          throws org.springframework.security.core.AuthenticationException {
    String authToken = request.getHeader(tokenHeader);
    String token = authToken.substring(7);
    return getUsernameFromToken(token);
  }

  public String getUsernameFromToken(String token) throws org.springframework.security.core.AuthenticationException {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public Date getIssuedAtDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getIssuedAt);
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) throws org.springframework.security.core.AuthenticationException {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) throws org.springframework.security.core.AuthenticationException {
    try {
      return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException
            | UnsupportedJwtException
            | MalformedJwtException
            | SignatureException
            | IllegalArgumentException e) {
      throw new AuthenticationException("Error parsing claims from token", e);
    }
  }

  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(clock.now());
  }

  private Boolean ignoreTokenExpiration(String token) {
    // here you specify tokens, for that the expiration is ignored
    return false;
  }

  public String generateToken(User userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return doGenerateToken(claims, userDetails.getEmail());
  }

  private String doGenerateToken(Map<String, Object> claims, String subject) {
    final Date createdDate = clock.now();
    final Date expirationDate = calculateExpirationDate(createdDate);

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(createdDate)
        .setExpiration(expirationDate).signWith(secretKey)
            .serializeToJsonWith(new GsonSerializer(gson)).compact();
  }

  public Boolean canTokenBeRefreshed(String token) {
    return (!isTokenExpired(token) || ignoreTokenExpiration(token));
  }

  public String refreshToken(String token) {
    final Date createdDate = clock.now();
    final Date expirationDate = calculateExpirationDate(createdDate);

    final Claims claims = getAllClaimsFromToken(token);
    claims.setIssuedAt(createdDate);
    claims.setExpiration(expirationDate);

    return Jwts.builder().setClaims(claims).signWith(secretKey).compact();
  }

  public Boolean validateToken(String token, User userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getEmail()) && !isTokenExpired(token));
  }

  private Date calculateExpirationDate(Date createdDate) {
    return new Date(createdDate.getTime() + expiration * 1000);
  }
}

