package com.rudraksha.shopsphere.review.service.impl;

import com.rudraksha.shopsphere.review.entity.Review;
import com.rudraksha.shopsphere.review.repository.ReviewRepository;
import com.rudraksha.shopsphere.review.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationServiceImpl implements ModerationService {

    private final ReviewRepository reviewRepository;

    // Simple profanity filter (in production, use a library or API)
    private static final Set<String> PROFANITIES = new HashSet<>(Arrays.asList(
            "badword1", "badword2", "badword3"
    ));

    // Common spam patterns
    private static final Set<String> SPAM_KEYWORDS = new HashSet<>(Arrays.asList(
            "click here", "buy now", "limited time", "promo code", "discount link"
    ));

    @Override
    public void moderateReview(Review review) {
        if (shouldAutoApprove(review)) {
            review.setModerationStatus(Review.ModerationStatus.APPROVED);
            log.info("Review {} auto-approved", review.getId());
        } else if (hasProfanity(review.getContent()) || hasProfanity(review.getTitle())) {
            review.setModerationStatus(Review.ModerationStatus.FLAGGED);
            log.warn("Review {} flagged for profanity", review.getId());
        } else if (isSpam(review.getContent())) {
            review.setModerationStatus(Review.ModerationStatus.FLAGGED);
            log.warn("Review {} flagged as spam", review.getId());
        } else {
            review.setModerationStatus(Review.ModerationStatus.PENDING);
            log.info("Review {} pending manual review", review.getId());
        }

        reviewRepository.save(review);
    }

    @Override
    public boolean hasProfanity(String text) {
        if (text == null) return false;
        
        String lowerText = text.toLowerCase();
        return PROFANITIES.stream().anyMatch(lowerText::contains);
    }

    @Override
    public boolean isSpam(String text) {
        if (text == null) return false;
        
        String lowerText = text.toLowerCase();
        
        // Check for spam keywords
        long spamKeywordCount = SPAM_KEYWORDS.stream()
                .filter(lowerText::contains)
                .count();
        
        // Check for excessive URLs
        long urlCount = text.split("http").length - 1;
        
        // Check for excessive capital letters
        long capitalCount = text.chars().filter(Character::isUpperCase).count();
        double capitalRatio = text.length() > 0 ? (double) capitalCount / text.length() : 0;
        
        return spamKeywordCount > 2 || urlCount > 2 || capitalRatio > 0.5;
    }

    @Override
    public boolean shouldAutoApprove(Review review) {
        // Auto-approve if verified purchase with reasonable content
        return review.getVerifiedPurchase() &&
                review.getTitle().length() >= 5 &&
                review.getContent().length() >= 20 &&
                !hasProfanity(review.getContent()) &&
                !isSpam(review.getContent());
    }
}
