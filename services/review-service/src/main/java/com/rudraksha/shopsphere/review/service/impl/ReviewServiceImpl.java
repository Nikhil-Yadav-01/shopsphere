package com.rudraksha.shopsphere.review.service.impl;

import com.rudraksha.shopsphere.review.dto.ReviewCreateRequest;
import com.rudraksha.shopsphere.review.dto.ReviewResponse;
import com.rudraksha.shopsphere.review.entity.Review;
import com.rudraksha.shopsphere.review.exception.*;
import com.rudraksha.shopsphere.review.mapper.ReviewMapper;
import com.rudraksha.shopsphere.review.repository.ReviewRepository;
import com.rudraksha.shopsphere.review.service.ModerationService;
import com.rudraksha.shopsphere.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ModerationService moderationService;
    private final ReviewMapper reviewMapper;

    @Override
    public ReviewResponse createReview(ReviewCreateRequest request, Long userId) {
        // Check if user already reviewed this product
        if (reviewRepository.existsByProductIdAndUserIdAndDeletedFalse(request.getProductId(), userId)) {
            throw new DuplicateReviewException("User has already reviewed this product");
        }

        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(userId)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .helpfulCount(0)
                .unhelpfulCount(0)
                .verifiedPurchase(true)
                .moderationStatus(Review.ModerationStatus.PENDING)
                .build();

        Review saved = reviewRepository.save(review);
        
        // Trigger moderation asynchronously (can use @Async)
        moderationService.moderateReview(saved);
        
        log.info("Review created for product {} by user {}", request.getProductId(), userId);
        return reviewMapper.reviewToReviewResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + id));
        return reviewMapper.reviewToReviewResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByProductIdAndModerationStatusAndDeletedFalse(
                productId, Review.ModerationStatus.APPROVED, pageable);
        
        List<ReviewResponse> content = page.getContent().stream()
                .map(reviewMapper::reviewToReviewResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByModerationStatusAndDeletedFalse(
                Review.ModerationStatus.APPROVED, pageable);
        
        List<ReviewResponse> content = page.getContent().stream()
                .filter(r -> r.getUserId().equals(userId))
                .map(reviewMapper::reviewToReviewResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPendingReviews(Pageable pageable) {
        Page<Review> page = reviewRepository.findByModerationStatusAndDeletedFalse(
                Review.ModerationStatus.PENDING, pageable);
        
        List<ReviewResponse> content = page.getContent().stream()
                .map(reviewMapper::reviewToReviewResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public ReviewResponse updateReview(Long id, ReviewCreateRequest request, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + id));

        // Only owner can update
        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setModerationStatus(Review.ModerationStatus.PENDING);

        Review updated = reviewRepository.save(review);
        log.info("Review {} updated by user {}", id, userId);
        
        return reviewMapper.reviewToReviewResponse(updated);
    }

    @Override
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + id));

        // Only owner can delete
        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewException("You can only delete your own reviews");
        }

        review.setDeleted(true);
        reviewRepository.save(review);
        log.info("Review {} deleted by user {}", id, userId);
    }

    @Override
    public void approveReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + id));
        
        review.setModerationStatus(Review.ModerationStatus.APPROVED);
        reviewRepository.save(review);
        log.info("Review {} approved", id);
    }

    @Override
    public void rejectReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + id));
        
        review.setModerationStatus(Review.ModerationStatus.REJECTED);
        reviewRepository.save(review);
        log.info("Review {} rejected", id);
    }

    @Override
    public void flagReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + id));
        
        review.setModerationStatus(Review.ModerationStatus.FLAGGED);
        reviewRepository.save(review);
        log.info("Review {} flagged for manual review", id);
    }

    @Override
    public void markHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));
        
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    public void markUnhelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));
        
        review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getProductAverageRating(Long productId) {
        Double avgRating = reviewRepository.getAverageRating(productId);
        return avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getProductReviewCount(Long productId) {
        return reviewRepository.getReviewCount(productId);
    }
}
