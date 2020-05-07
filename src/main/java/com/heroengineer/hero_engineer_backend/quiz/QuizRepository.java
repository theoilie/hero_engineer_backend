package com.heroengineer.hero_engineer_backend.quiz;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for querying Quiz documents.
 */
public interface QuizRepository extends MongoRepository<Quiz, String> {

}
