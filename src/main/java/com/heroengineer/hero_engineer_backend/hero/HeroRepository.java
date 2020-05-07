package com.heroengineer.hero_engineer_backend.hero;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for querying Hero documents.
 */
public interface HeroRepository extends MongoRepository<Hero, String> {

    Hero findByNameIgnoreCase(String name);
}
