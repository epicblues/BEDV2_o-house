package com.prgrms.ohouse.web.user.api;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.prgrms.ohouse.domain.common.security.AuthUtility;
import com.prgrms.ohouse.domain.user.application.FollowService;
import com.prgrms.ohouse.domain.user.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class FollowRestControllerTest {

	@MockBean
	private FollowService followService;
	@MockBean
	private AuthUtility authUtility;

	@Autowired
	private FollowRestController followRestController;

	private MockMvc mockMvc;
	private Long userId = 1L;
	Long authUserId = 10L;

	User user = spy(
		User.builder()
		.nickname("guest")
		.email("guest@gmail.com")
		.password("guestPassword12").build()
	);

	@BeforeEach
	void setup() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(followRestController)
			.setControllerAdvice(FollowApiExceptionHandler.class)
			.alwaysDo(print())
			.build();

		when(authUtility.getAuthUser()).thenReturn(user);
		when(user.getId()).thenReturn(Long.valueOf(authUserId));
	}

	@Test
	void followTest() throws Exception {
		doNothing().when(followService).followUser(authUserId, userId);

		mockMvc.perform(post("/api/v0/user/{userId}/follow", userId))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("User Follow Success")));
	}

	@Test
	void unfollowTest() throws Exception {

		doNothing().when(followService).unfollowUser(authUserId, userId);

		mockMvc.perform(delete("/api/v0/user/{userId}/follow", userId))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("User Unfollow Success")));
	}

}