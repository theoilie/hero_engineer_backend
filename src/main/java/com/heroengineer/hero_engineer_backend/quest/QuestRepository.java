package com.heroengineer.hero_engineer_backend.quest;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for querying Quest documents.
 */
public interface QuestRepository extends MongoRepository<Quest, String> {

}
