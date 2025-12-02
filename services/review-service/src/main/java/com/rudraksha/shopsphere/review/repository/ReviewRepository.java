package com.rudraksha.shopsphere.review.repository;

import com.rudraksha.shopsphere.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** Soft-delete aware findById **/
    Optional<Review> findByIdAndDeletedFalse(Long id);

    /** Get reviews for product (only approved + not deleted) **/
    Page<Review> findByProductIdAndModerationStatusAndDeletedFalse(
            Long productId,
            Review.ModerationStatus status,
            Pageable pageable
    );

    /** Get reviews created by a user (not deleted) **/
    Page<Review> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    /** Get pending/approved/rejected reviews (not deleted) **/
    Page<Review> findByModerationStatusAndDeletedFalse(
            Review.ModerationStatus status,
            Pageable pageable
    );

    /** Rating aggregation **/
    @Query("""
           SELECT AVG(r.rating) 
           FROM Review r 
           WHERE r.productId = :productId 
             AND r.moderationStatus = 'APPROVED'
             AND r.deleted = false
           """)
    Double getAverageRating(Long productId);

    /** Review count **/
    @Query("""
           SELECT COUNT(r) 
           FROM Review r 
           WHERE r.productId = :productId 
             AND r.moderationStatus = 'APPROVED'
             AND r.deleted = false
           """)
    Long getReviewCount(Long productId);

    /** Prevent duplicate reviews for same user+product **/
    boolean existsByProductIdAndUserIdAndDeletedFalse(Long productId, Long userId);
}
