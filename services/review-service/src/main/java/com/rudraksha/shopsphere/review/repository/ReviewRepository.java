package com.rudraksha.shopsphere.review.repository;

import com.rudraksha.shopsphere.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndModerationStatusAndDeletedFalse(
            Long productId, Review.ModerationStatus status, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.moderationStatus = 'APPROVED' AND r.deleted = false")
    Double getAverageRating(Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.moderationStatus = 'APPROVED' AND r.deleted = false")
    Long getReviewCount(Long productId);

    List<Review> findByUserIdAndDeletedFalse(Long userId);

    Page<Review> findByModerationStatusAndDeletedFalse(Review.ModerationStatus status, Pageable pageable);

    boolean existsByProductIdAndUserIdAndDeletedFalse(Long productId, Long userId);
}
