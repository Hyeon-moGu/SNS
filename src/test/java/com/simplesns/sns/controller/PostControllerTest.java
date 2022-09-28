package com.simplesns.sns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplesns.sns.controller.request.PostCommentRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @DisplayName("포스트 삭제")
    @Test
    @WithMockUser
    void givenNothing_whenRequestDeletePost_thenReturnSuccess() throws Exception{
        // When & Then
        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("포스트 삭제 - 실패 (로그인을 하지 않음)")
    @Test
    @WithAnonymousUser
    void givenNothing_whenRequestDeletePostNotLogin_thenReturnError() throws Exception{
        // When & Then
        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("포스트 삭제  - 실패 (작성자와 본인이 다르면 에러발생)")
    @Test
    @WithMockUser
    void givenNothing_whenRequestDeletePostOtherUser_thenReturnError() throws Exception{
        // mocking
        doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService).delete(any(), any());

        // When & Then
        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("포스트 삭제 - 실패 (삭제하려는 글이 없으면 에러발생)")
    @Test
    @WithMockUser
    void givenNothing_whenRequestDeleteNonePost_thenReturnError() throws Exception{
        // mocking
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).delete(any(), any());

        // When & Then
        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @DisplayName("피드목록 요청")
    @Test
    @WithMockUser
    void givenNothing_whenRequestFeedList_thenReturnSuccess() throws Exception{
        when(postService.list(any())).thenReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("피드목록 요청 - 실패 (로그인을 하지 않음)")
    @Test
    @WithAnonymousUser
    void givenNothing_whenRequestFeedList_thenReturnError() throws Exception{
        when(postService.list(any())).thenReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("나의 피드목록 요청")
    @Test
    @WithMockUser
    void givenNothing_whenRequestMyFeedList_thenReturnSuccess() throws Exception{
        when(postService.my(any(), any())).thenReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/posts/my")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("나의 피드목록 요청 - 실패 (로그인을 하지 않음)")
    @Test
    @WithAnonymousUser
    void givenNothing_whenRequestMyFeedList_thenReturnError() throws Exception{
        when(postService.my(any(), any())).thenReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/posts/my")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("좋아요 기능")
    @Test
    @WithMockUser
    void givenNothing_whenRequestLike_thenNothing() throws Exception{
        // When & Then
        mockMvc.perform(post("/api/v1/posts/1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("좋아요 기능 - 실패 (로그인을 하지 않음)")
    @Test
    @WithAnonymousUser
    void givenNothing_whenRequestLikeNoneLogin_thenReturnError() throws Exception{
        // When & Then
        mockMvc.perform(post("/api/v1/posts/1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("좋아요 기능 - 실패 (게시물이 없는 경우)")
    @Test
    @WithMockUser
    void givenNothing_whenRequestLikeNonePost_thenReturnError() throws Exception{
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).like(any(), any());

        // When & Then
        mockMvc.perform(post("/api/v1/posts/1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @DisplayName("댓글 작성")
    @Test
    @WithMockUser
    void givenNothing_whenRequestComment_thenNothing() throws Exception{
        // When & Then
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("댓글 작성 - 실패 (로그인을 하지 않음)")
    @Test
    @WithAnonymousUser
    void givenNothing_whenRequestCommentNoneLogin_thenReturnError() throws Exception{
        // When & Then
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("댓글 작성 - 실패 (게시물이 없는 경우)")
    @Test
    @WithMockUser
    void givenNothing_whenRequestCommentNonePost_thenReturnError() throws Exception{
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).comment(any(), any(), any());

        // When & Then
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }


}
