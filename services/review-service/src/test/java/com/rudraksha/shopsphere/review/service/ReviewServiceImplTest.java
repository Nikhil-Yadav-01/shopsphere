package com.rudraksha.shopsphere.review.service;

import com.rudraksha.shopsphere.review.dto.ReviewCreateRequest;
import com.rudraksha.shopsphere.review.dto.ReviewResponse;
import com.rudraksha.shopsphere.review.entity.Review;
import com.rudraksha.shopsphere.review.exception.*;
import com.rudraksha.shopsphere.review.mapper.ReviewMapper;
import com.rudraksha.shopsphere.review.repository.ReviewRepository;
import com.rudraksha.shopsphere.review.service.impl.ReviewServiceImpl;
import com.rudraksha.shopsphere.review.service.impl.ModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ModerationServiceImpl moderationService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review testReview;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        testReview = Review.builder()
                .id(1L)
                .productId(100L)
                .userId(1L)
                .rating(5)
                .title("Great Product")
                .content("This product exceeded my expectations!")
                .moderationStatus(Review.ModerationStatus.APPROVED)
                .verifiedPurchase(true)
                .build();

        reviewResponse = ReviewResponse.builder()
                .id(1L)
                .productId(100L)
                .userId(1L)
                .rating(5)
                .title("Great Product")
                .moderationStatus(Review.ModerationStatus.APPROVED)
                .build();
    }

    @Test
    void testCreateReview_Success() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productId(100L)
                .rating(5)
                .title("Great Product")
                .content("This product exceeded my expectations!")
                .build();

        when(reviewRepository.existsByProductIdAndUserIdAndDeletedFalse(100L, 1L))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class)))
                .thenReturn(testReview);
        when(reviewMapper.reviewToReviewResponse(any()))
                .thenReturn(reviewResponse);

        ReviewResponse result = reviewService.createReview(request, 1L);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(moderationService, times(1)).moderateReview(any());
    }

    @Test
    void testCreateReview_DuplicateReview() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productId(100L)
                .rating(5)
                .title("Great Product")
                .content("This product exceeded my expectations!")
                .build();

        when(reviewRepository.existsByProductIdAndUserIdAndDeletedFalse(100L, 1L))
                .thenReturn(true);

        assertThrows(DuplicateReviewException.class, () -> reviewService.createReview(request, 1L));
    }

    @Test
    void testUpdateReview_Authorized() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productId(100L)
                .rating(4)
                .title("Good Product")
                .content("Still good")
                .build();

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class)))
                .thenReturn(testReview);
        when(reviewMapper.reviewToReviewResponse(any()))
                .thenReturn(reviewResponse);

        ReviewResponse result = reviewService.updateReview(1L, request, 1L);

        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testUpdateReview_Unauthorized() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productId(100L)
                .rating(4)
                .title("Good Product")
                .content("Still good")
                .build();

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(testReview));

        assertThrows(UnauthorizedReviewException.class, () -> reviewService.updateReview(1L, request, 999L));
    }

    @Test
    void testDeleteReview_Authorized() {
        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(testReview));

        reviewService.deleteReview(1L, 1L);

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testDeleteReview_Unauthorized() {
        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(testReview));

        assertThrows(UnauthorizedReviewException.class, () -> reviewService.deleteReview(1L, 999L));
    }

    @Test
    void testApproveReview() {
        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(testReview));

        reviewService.approveReview(1L);

        assertEquals(Review.ModerationStatus.APPROVED, testReview.getModerationStatus());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testMarkHelpful() {
        testReview.setHelpfulCount(0);
        
        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(testReview));

        reviewService.markHelpful(1L);

        assertEquals(1, testReview.getHelpfulCount());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }
}
