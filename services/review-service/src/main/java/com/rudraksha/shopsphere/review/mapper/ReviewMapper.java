package com.rudraksha.shopsphere.review.mapper;

import com.rudraksha.shopsphere.review.dto.ReviewResponse;
import com.rudraksha.shopsphere.review.entity.Review;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ReviewMapper {

    private final ModelMapper modelMapper;

    public ReviewMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ReviewResponse reviewToReviewResponse(Review review) {
        return modelMapper.map(review, ReviewResponse.class);
    }

    public Review reviewResponseToReview(ReviewResponse reviewResponse) {
        return modelMapper.map(reviewResponse, Review.class);
    }
}
