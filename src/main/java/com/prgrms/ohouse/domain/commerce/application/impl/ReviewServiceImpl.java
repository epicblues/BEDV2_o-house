package com.prgrms.ohouse.domain.commerce.application.impl;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prgrms.ohouse.domain.commerce.application.ReviewService;
import com.prgrms.ohouse.domain.commerce.application.command.ReviewRegisterCommand;
import com.prgrms.ohouse.domain.commerce.model.product.Product;
import com.prgrms.ohouse.domain.commerce.model.product.ProductRepository;
import com.prgrms.ohouse.domain.commerce.model.review.Review;
import com.prgrms.ohouse.domain.commerce.model.review.ReviewRegisterFailException;
import com.prgrms.ohouse.domain.commerce.model.review.ReviewRepository;
import com.prgrms.ohouse.domain.common.file.FileManager;
import com.prgrms.ohouse.domain.common.file.UploadFile;
import com.prgrms.ohouse.domain.user.model.User;
import com.prgrms.ohouse.domain.user.model.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewServiceImpl implements ReviewService {
	private final ProductRepository productRepository;
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final FileManager fileManager;

	@Transactional
	@Override
	public Long registerReview(ReviewRegisterCommand command) {
		Product product = productRepository.findById(command.getProductId())
			.orElseThrow(() -> new ReviewRegisterFailException("invalid product id"));
		User user = userRepository.findById(command.getUserId())
			.orElseThrow(() -> new ReviewRegisterFailException("invalid user id"));
		Review review;
		if (command.isPhotoReview()) {
			try {
				UploadFile reviewImage = fileManager.storeFile(command.getReviewImage());
				review = Review.createPhotoReview(product, user, command.getReviewPoint(), command.getContents(),
					reviewImage.getUploadFileUrl());
			} catch (IOException e) {
				throw new ReviewRegisterFailException(e.getMessage(), e);
			}
		} else {
			review = Review.createNormalReview(product, user, command.getReviewPoint(), command.getContents());
		}
		return reviewRepository.save(review).getId();
	}
}
