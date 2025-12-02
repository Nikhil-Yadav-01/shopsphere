package com.rudraksha.shopsphere.review.service;

import com.rudraksha.shopsphere.review.dto.ReviewCreateRequest;
import com.rudraksha.shopsphere.review.dto.ReviewResponse;
import com.rudraksha.shopsphere.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse createReview(ReviewCreateRequest request, Long userId);

    ReviewResponse getReview(Long id);

    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable);

    Page<ReviewResponse> getPendingReviews(Pageable pageable);

    ReviewResponse updateReview(Long id, ReviewCreateRequest request, Long userId);

    void deleteReview(Long id, Long userId);

    void approveReview(Long id);

    void rejectReview(Long id);

    void flagReview(Long id);

    void markHelpful(Long reviewId);

    void markUnhelpful(Long reviewId);

    Double getProductAverageRating(Long productId);

    Long getProductReviewCount(Long productId);
}
