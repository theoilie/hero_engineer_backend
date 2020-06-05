package com.heroengineer.hero_engineer_backend.section;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for querying Section documents.
 */
public interface SectionRepository extends MongoRepository<Section, String> {
}
