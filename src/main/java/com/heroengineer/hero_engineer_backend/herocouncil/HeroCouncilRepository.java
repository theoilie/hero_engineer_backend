package com.heroengineer.hero_engineer_backend.herocouncil;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for querying HeroCouncil documents.
 */
public interface HeroCouncilRepository extends MongoRepository<HeroCouncil, String> {
}
