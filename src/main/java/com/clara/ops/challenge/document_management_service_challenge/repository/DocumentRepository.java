package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for managing Document entities in the database.
 */
@Repository
public interface DocumentRepository extends ReactiveCrudRepository<Document, Long> {

    /**
     * Finds documents based on a set of filters with pagination.
     *
     * @param user   The user ID to filter by.
     * @param name   The name of the document to filter by (case-insensitive, partial match).
     * @param tags   An array of tags to filter by.
     * @param limit  The maximum number of results to return.
     * @param offset The starting offset for pagination.
     * @return A Flux of Document entities matching the criteria.
     */
    @Query("SELECT * FROM documents WHERE " +
            "(:user IS NULL OR user_id = :user) AND " +
            "(:name IS NULL OR LOWER(document_name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:tags IS NULL OR :tags <@ tags) " +
            "ORDER BY created_at DESC " +
            "LIMIT :limit OFFSET :offset")
    Flux<Document> findByFilters(@Param("user") String user,
                                 @Param("name") String name,
                                 @Param("tags") String[] tags,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    /**
     * Counts the total number of documents matching a set of filters.
     *
     * @param user The user ID to filter by.
     * @param name The name of the document to filter by (case-insensitive, partial match).
     * @param tags An array of tags to filter by.
     * @return A Mono emitting the total count of matching documents.
     */
    @Query("SELECT COUNT(*) FROM documents WHERE " +
            "(:user IS NULL OR user_id = :user) AND " +
            "(:name IS NULL OR LOWER(document_name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:tags IS NULL OR :tags <@ tags)")
    Mono<Long> countByFilters(@Param("user") String user,
                              @Param("name") String name,
                              @Param("tags") String[] tags);
}
