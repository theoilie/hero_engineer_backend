package com.heroengineer.hero_engineer_backend.user;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for querying UserWhitelist documents.
 */
public interface UserWhitelistRepository extends MongoRepository<UserWhitelist, String> {

}
