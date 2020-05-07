package com.heroengineer.hero_engineer_backend.user;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for querying User documents.
 */
public interface UserRepository extends MongoRepository<User, String> {

    User findByEmailIgnoreCase(String email);
    User findByUsernameIgnoreCase(String username);

}
