package com.rudraksha.shopsphere.review.service;

import com.rudraksha.shopsphere.review.entity.Review;

public interface ModerationService {
    
    void moderateReview(Review review);
    
    boolean hasProfanity(String text);
    
    boolean isSpam(String text);
    
    boolean shouldAutoApprove(Review review);
}
