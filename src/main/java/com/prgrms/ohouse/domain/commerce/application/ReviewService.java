package com.prgrms.ohouse.domain.commerce.application;

import org.springframework.data.domain.Pageable;

import com.prgrms.ohouse.domain.commerce.application.command.ReviewRegisterCommand;
import com.prgrms.ohouse.domain.commerce.model.product.Product;
import com.prgrms.ohouse.domain.commerce.model.review.PagedPhotoReviewInformation;
import com.prgrms.ohouse.domain.commerce.model.review.PagedReviewInformation;

public interface ReviewService {

	Long registerReview(ReviewRegisterCommand command);

	PagedReviewInformation loadAllProductReviews(Pageable pageable, Product product);

	PagedPhotoReviewInformation loadOnlyPhotoReviews(Pageable pageable, Product product);
}
