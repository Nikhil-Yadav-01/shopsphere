package com.rudraksha.shopsphere.review.mapper;

import com.rudraksha.shopsphere.review.dto.ReviewResponse;
import com.rudraksha.shopsphere.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {
    
    ReviewResponse reviewToReviewResponse(Review review);
    
    Review reviewResponseToReview(ReviewResponse reviewResponse);
}
