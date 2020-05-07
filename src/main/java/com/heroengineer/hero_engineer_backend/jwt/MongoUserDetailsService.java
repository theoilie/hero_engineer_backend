package com.heroengineer.hero_engineer_backend.jwt;

import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MongoUserDetailsService implements UserDetailsService {

  private final UserRepository userRepo;

  @Autowired
  public MongoUserDetailsService(UserRepository userRepo) {
    this.userRepo = userRepo;
  }

  @Override
  public User loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepo.findByEmailIgnoreCase(username);

    if (user == null) {
      throw new UsernameNotFoundException(String.format("USER_NOT_FOUND '%s'.", username));
    }

    return user;
  }

}


