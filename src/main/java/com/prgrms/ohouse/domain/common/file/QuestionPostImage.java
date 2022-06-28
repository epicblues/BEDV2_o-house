package com.prgrms.ohouse.domain.common.file;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.prgrms.ohouse.domain.community.model.post.question.QuestionPost;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionPostImage extends StoredFile {

	public QuestionPostImage(String originalFileName, String url, QuestionPost questionPost) {
		super(originalFileName, url);
		this.questionPost = questionPost;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private QuestionPost questionPost;
}