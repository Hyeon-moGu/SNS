package com.simplesns.sns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplesns.sns.controller.request.PostCreateRequest;
import com.simplesns.sns.controller.request.PostModifyRequest;
import com.simplesns.sns.exception.ErrorCode;
import com.simplesns.sns.exception.SnsApplicationException;
import com.simplesns.sns.fixture.PostEntityFixture;
import com.simplesns.sns.model.Post;
import com.simplesns.sns.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @DisplayName("포스트 작성")
    @Test
    @WithMockUser
    void givenNothing_whenRequestPostLogin_thenReturnSuccess() throws Exception{
        // Given
        String title = "title";
        String body = "body";

        // When & Then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("포스트 작성 - 실패 (로그인을 하지 않음)")
    @Test
    @WithAnonymousUser
    void givenNothing_whenRequestPostNotLogin_thenReturnError() throws Exception{
        // Given
        String title = "title";
        String body = "body";

        // When & Then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("포스트 수정")
    @Test
    @WithMockUser
    void givenNothing_whenRequestModifyPostLogin_thenReturnSuccess() throws Exception{
        // Given
        String title = "title";
        String body = "body";

        when(postService.modify(eq(title), eq(body), any(), any())).
                thenReturn(Post.fromEntity(PostEntityFixture.get("username", 1, 1)));

        // When & Then
        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("포스트 수정 - 실패 (로그인을 하지 않음)")
    @Test
    @WithAnonymousUser
    void givenNothing_whenRequestModifyPostNotLogin_thenReturnError() throws Exception{
        // Given
        String title = "title";
        String body = "body";

        // When & Then
        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("포스트 수정 - 실패 (작성자와 본인이 다르면 에러발생)")
    @Test
    @WithMockUser
    void givenNothing_whenRequestModifyPostOtherUser_thenReturnError() throws Exception{
        // Given
        String title = "title";
        String body = "body";

        doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService).modify(eq(title), eq(body), any(), eq(1));

        // When & Then
        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("포스트 수정 - 실패 (수정하려는 글이 없으면 에러발생)")
    @Test
    @WithMockUser
    void givenNothing_whenRequestModifyNonePost_thenReturnError() throws Exception{
        // Given
        String title = "title";
        String body = "body";

        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).modify(eq(title), eq(body), any(), eq(1));

        // When & Then
        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }


}
