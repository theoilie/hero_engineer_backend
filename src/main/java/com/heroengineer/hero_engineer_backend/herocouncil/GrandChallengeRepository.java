package com.heroengineer.hero_engineer_backend.herocouncil;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository for querying GrandChallenge documents.
 */
public interface GrandChallengeRepository extends MongoRepository<GrandChallenge, String> {
    Optional<GrandChallenge> findByCode(String code);
}
