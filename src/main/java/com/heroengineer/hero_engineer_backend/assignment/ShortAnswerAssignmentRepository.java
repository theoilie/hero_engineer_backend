package com.heroengineer.hero_engineer_backend.assignment;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for querying ShortAnswerAssignment documents.
 */
public interface ShortAnswerAssignmentRepository extends MongoRepository<ShortAnswerAssignment, String> {
}
