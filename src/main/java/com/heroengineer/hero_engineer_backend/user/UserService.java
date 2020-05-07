package com.heroengineer.hero_engineer_backend.user;

import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class UserService {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepo;

    @Value("${jwt.http.request.header}")
    private String tokenHeader;

    @Autowired
    public UserService(JwtTokenUtil jwtTokenUtil, UserRepository userRepo) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepo = userRepo;
    }

    public boolean isProf(HttpServletRequest request) {
        String authToken = request.getHeader(tokenHeader);
        final String token = authToken.substring(7);
        String email = jwtTokenUtil.getUsernameFromToken(token);

        return email != null && userRepo.findByEmailIgnoreCase(email).isProf;
    }
}
