package com.heroengineer.hero_engineer_backend.jwt;

import java.util.Collections;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserWhitelist;
import com.heroengineer.hero_engineer_backend.user.UserWhitelistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("${origins}")
public class JwtAuthenticationRestController {

  @Value("${jwt.http.request.header}")
  private String tokenHeader;

  private final AuthenticationManager authenticationManager;
  private final JwtTokenUtil jwtTokenUtil;
  private final MongoUserDetailsService mongoUserDetailsService;
  private final UserWhitelistRepository userWhitelistRepo;

  @Autowired
  public JwtAuthenticationRestController(AuthenticationManager authenticationManager,
                                         JwtTokenUtil jwtTokenUtil,
                                         MongoUserDetailsService mongoUserDetailsService,
                                         UserWhitelistRepository userWhitelistRepo) {
    this.authenticationManager = authenticationManager;
    this.jwtTokenUtil = jwtTokenUtil;
    this.mongoUserDetailsService = mongoUserDetailsService;
    this.userWhitelistRepo = userWhitelistRepo;
  }

  @RequestMapping(value = "${jwt.get.token.uri}", method = RequestMethod.POST)
  public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtTokenRequest authenticationRequest)
      throws AuthenticationException {
    UserWhitelist whitelist = userWhitelistRepo
            .findById("default")
            .orElse(new UserWhitelist("default", Collections.singletonList("admin@usc.edu")));
    if (!whitelist.getEmails().contains(authenticationRequest.getUsername())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"The email address you gave is not on the registrar for this semester. Please try a different email or contact Professor Ramsey.\"}");
    }

    authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

    final User userDetails = mongoUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());

    final String token = jwtTokenUtil.generateToken(userDetails);

    return ResponseEntity.ok(new JwtTokenResponse(token));
  }

  @RequestMapping(value = "${jwt.refresh.token.uri}", method = RequestMethod.GET)
  public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
    String authToken = request.getHeader(tokenHeader);
    final String token = authToken.substring(7);
    String username = jwtTokenUtil.getUsernameFromToken(token);

    if (username == null) {
      return ResponseEntity.badRequest().body("{\"error\": \"JWT not accepted\"}");
    }

    UserDetails user = mongoUserDetailsService.loadUserByUsername(username);

    if (jwtTokenUtil.canTokenBeRefreshed(token)) {
      String refreshedToken = jwtTokenUtil.refreshToken(token);
      return ResponseEntity.ok(new JwtTokenResponse(refreshedToken));
    } else {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @ExceptionHandler({ AuthenticationException.class })
  public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
  }

  private void authenticate(String username, String password) {
    Objects.requireNonNull(username);
    Objects.requireNonNull(password);

    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    } catch (DisabledException e) {
      throw new AuthenticationException("USER_DISABLED", e);
    } catch (BadCredentialsException e) {
      throw new AuthenticationException("INVALID_CREDENTIALS", e);
    }
  }
}

