package com.prgrms.ohouse.domain.community.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import com.prgrms.ohouse.domain.community.application.command.CreateHousewarmingPostCommand;
import com.prgrms.ohouse.domain.community.application.impl.HousewarmingPostServiceImpl;
import com.prgrms.ohouse.domain.community.model.housewarming.Budget;
import com.prgrms.ohouse.domain.community.model.housewarming.Family;
import com.prgrms.ohouse.domain.community.model.housewarming.HousewarmingPost;
import com.prgrms.ohouse.domain.community.model.housewarming.HousewarmingPostRepository;
import com.prgrms.ohouse.domain.community.model.housewarming.HousingType;
import com.prgrms.ohouse.domain.community.model.housewarming.WorkMetadata;
import com.prgrms.ohouse.domain.community.model.housewarming.WorkerType;
import com.prgrms.ohouse.infrastructure.TestDataProvider;

@SpringBootTest
@Transactional
@DisplayName("HousewarmingPostService는")
class HousewarmingPostServiceImplTest {

	@Autowired
	private HousewarmingPostServiceImpl housewarmingPostServiceImpl;

	@Autowired
	private HousewarmingPostRepository housewarmingPostRepository;

	@Autowired
	private TestDataProvider fixtureProvider;

	@PersistenceContext
	private EntityManager em;

	@Test
	@DisplayName("post 생성 요청을 받아서 post를 생성하고 영속화한다.")
	void persist_post_entity_to_database() {
		// TODO: 선택 필드 테스트 케이스

		// Given
		var user = fixtureProvider.insertGuestUser("guest");
		var command = CreateHousewarmingPostCommand.builder()
			.title("test1")
			.content("test1content")
			.housingType(HousingType.APARTMENT)
			.area(2L)
			.budget(new Budget(100, 150))
			.family(new Family("SINGLE", null, null))
			.workMetadata(WorkMetadata.builder().workerType(WorkerType.valueOf("SELF")).build())
			.links(Collections.emptyList())
			.build();

		// When
		Long postId = housewarmingPostServiceImpl.createPost(user.getId(), command, Collections.emptyList());

		// Then
		var createdPost = housewarmingPostRepository.findById(postId);
		assertThat(createdPost).isNotEmpty();
		assertThat(createdPost.get().getUser().getId()).isEqualTo(user.getId());
		assertThat(createdPost.get()).extracting("title").isEqualTo("test1");
		assertThat(createdPost.get().getBudget().getTotal()).isEqualTo(250L);
		assertThat(createdPost.get().getWorkMetadata().getWorkerType()).isEqualTo(WorkerType.SELF);

	}

	@Test
	@DisplayName("사용자의 권한이 있는 집들이 게시물을 삭제한다. - 성공")
	void delete_authorized_housewarming_content() {

		// Given
		var persistedUserWithToken = fixtureProvider.insertGuestUser("guest");
		var persistedPost = fixtureProvider.insertHousewarmingPostWithAuthor(persistedUserWithToken, 1);
		var postId = persistedPost.getId();
		var authorId = persistedPost.getUser().getId();

		// When
		housewarmingPostServiceImpl.deletePost(authorId, postId);

		// Then
		assertThat(housewarmingPostRepository.findById(postId)).isEmpty();
	}

	@Test
	@DisplayName("사용자의 권한이 없을 경우 게시물 권한 없음 예외를 던진다.")
	void throws_unauthorized_content_exception() {

		// Given
		var persistedUserWithToken = fixtureProvider.insertGuestUser("guest");
		var persistedPost = fixtureProvider.insertHousewarmingPostWithAuthor(persistedUserWithToken, 1);
		var postId = persistedPost.getId();
		var unauthorizedId = persistedPost.getUser().getId() + 4123;

		assertThatThrownBy(() -> {
			housewarmingPostServiceImpl.deletePost(unauthorizedId, postId);
		}).isInstanceOf(UnauthorizedContentAccessException.class);

	}

	@Test
	@DisplayName("postId를 통해 저장된 집들이 정보를 가져온다.")
	void query_persisted_housewarming_post_by_post_id() {

		// Given
		var author = fixtureProvider.insertGuestUser("guest");
		var savedPost = fixtureProvider.insertHousewarmingPostWithAuthor(author, 1);

		// When
		var queriedPostResult = housewarmingPostServiceImpl.getSinglePost(savedPost.getId());

		// Then
		assertThat(queriedPostResult.getPostId()).isEqualTo(savedPost.getId());
		assertThat(queriedPostResult.getContent()).isEqualTo(savedPost.getContent());
		assertThat(queriedPostResult.getImages()).isEqualTo(savedPost.getImages());

	}

	@Test
	@DisplayName("원하는 단일 집들이 게시물의 조회수를 1 증가 시킨다.")
	void increment_visit_count_by_1() {

		// Given
		var author = fixtureProvider.insertGuestUser("guest");
		var savedPost = fixtureProvider.insertHousewarmingPostWithAuthor(author, 1);

		// When
		housewarmingPostServiceImpl.updateViews(savedPost.getId());
		em.flush();
		em.clear();

		// Then
		var updatedPost = housewarmingPostRepository.findById(savedPost.getId()).orElseThrow();
		assertThat(updatedPost.getVisitCount()).isEqualTo(1);

	}

	@Test
	@DisplayName("사용자가 요청에 맞춘 액수의 집들이 컨텐츠와 반환해야 한다. ")
	void return_proper_post_with_proper_size_page() {
		var author = fixtureProvider.insertGuestUser("guest");
		for (int i = 1; i <= 20; i++) {
			fixtureProvider.insertHousewarmingPostWithAuthor(author, i);
		}

		// Given
		PageRequest pageRequest = PageRequest.of(0, 19);

		// When
		Slice<HousewarmingPost> result = housewarmingPostServiceImpl.getPosts(pageRequest);

		// Then
		assertThat(result.getSize()).isEqualTo(19);
		assertThat(result.getContent()).hasSize(19);
		assertThat(result.hasNext()).isTrue();

	}
}
