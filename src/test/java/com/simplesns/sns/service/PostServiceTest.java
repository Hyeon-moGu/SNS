package com.simplesns.sns.service;

import com.simplesns.sns.exception.ErrorCode;
import com.simplesns.sns.exception.SnsApplicationException;
import com.simplesns.sns.fixture.PostEntityFixture;
import com.simplesns.sns.fixture.UserEntityFixture;
import com.simplesns.sns.model.User;
import com.simplesns.sns.model.entity.PostEntity;
import com.simplesns.sns.model.entity.UserEntity;
import com.simplesns.sns.repository.PostEntityRepository;
import com.simplesns.sns.repository.UserEntityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest

public class PostServiceTest {

    @Autowired
    private PostService postService;

    @MockBean
    private PostEntityRepository postEntityRepository;

    @MockBean
    private UserEntityRepository userEntityRepository;

    @DisplayName("포스트 작성 정상 작동")
    @Test
    void givenNothing_whenRequestCreatePost_thenCreatePost() {
        // Given
        String title = "title";
        String body = "body";
        String username = "username";

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(mock(UserEntity.class)));
        when(postEntityRepository.save(any())).thenReturn(mock(PostEntity.class));

        Assertions.assertDoesNotThrow(() -> postService.create(title,body,username));
    }

    @DisplayName("요청한 유저가 존재하지 않아 포스트작성 실패")
    @Test
    void givenNothing_whenRequestCreatePostNoneUser_thenReturnError() {
        // Given
        String title = "title";
        String body = "body";
        String username = "username";

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(postEntityRepository.save(any())).thenReturn(mock(PostEntity.class));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.create(title,body,username));
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }

    @DisplayName("포스트 수정 정상 작동")
    @Test
    void givenNothing_whenRequestModifyPost_thenReturnOk() {
        // Given
        String title = "title";
        String body = "body";
        String username = "username";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(username,postId, 1);
        UserEntity userEntity = postEntity.getUser();

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity));
        when(postEntityRepository.saveAndFlush(any())).thenReturn(postEntity);

        Assertions.assertDoesNotThrow(() -> postService.modify(title,body,username, postId));
    }

    @DisplayName("포스트가 존재하지 않아 포스트수정 실패")
    @Test
    void givenNothing_whenRequestModifyNonePost_thenReturnError() {
        // Given
        String title = "title";
        String body = "body";
        String username = "username";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(username,postId, 1);
        UserEntity userEntity = postEntity.getUser();

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.empty());

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.modify(title,body,username, postId));
        Assertions.assertEquals(ErrorCode.POST_NOT_FOUND, e.getErrorCode());
    }

    @DisplayName("포스트 수정 권한이 없어 포스트수정 실패")
    @Test
    void givenNothing_whenRequestModifyUnauthorized_thenReturnError() {
        // Given
        String title = "title";
        String body = "body";
        String username = "username";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(username,postId, 1);
        UserEntity writer = UserEntityFixture.get("username1", "password1", 2);

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(writer));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.modify(title,body,username, postId));
        Assertions.assertEquals(ErrorCode.INVALID_PERMISSION, e.getErrorCode());
    }

    @DisplayName("포스트 삭제 정상 작동")
    @Test
    void givenNothing_whenRequestDeletePost_thenReturnOk() {
        // Given
        String username = "username";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(username,postId, 1);
        UserEntity userEntity = postEntity.getUser();

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity));

        Assertions.assertDoesNotThrow(() -> postService.delete(username, 1));
    }

    @DisplayName("포스트가 존재하지 않아 포스트삭제 실패")
    @Test
    void givenNothing_whenRequestDeleteNonePost_thenReturnError() {
        // Given
        String username = "username";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(username,postId, 1);
        UserEntity userEntity = postEntity.getUser();

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.empty());

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.delete(username, 1));
        Assertions.assertEquals(ErrorCode.POST_NOT_FOUND, e.getErrorCode());
    }

    @DisplayName("포스트 삭제 권한이 없어 포스트삭제 실패")
    @Test
    void givenNothing_whenRequestDeleteUnauthorized_thenReturnError() {
        // Given
        String username = "username";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(username,postId, 1);
        UserEntity writer = UserEntityFixture.get("username1", "password1", 2);

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(writer));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.delete(username, 1));
        Assertions.assertEquals(ErrorCode.INVALID_PERMISSION, e.getErrorCode());
    }

    @DisplayName("피드목록 요청 정상 작동")
    @Test
    void givenNothing_whenRequestFeedList_thenReturnOk() {
        Pageable pageable = mock(Pageable.class);

        // When & Then
        when(postEntityRepository.findAll(pageable)).thenReturn(Page.empty());
        Assertions.assertDoesNotThrow(() -> postService.list(pageable));
    }

    @DisplayName("나의 피드목록 요청 정상 작동")
    @Test
    void givenNothing_whenRequestMyFeedList_thenReturnOk() {
        Pageable pageable = mock(Pageable.class);
        UserEntity user = mock(UserEntity.class);

        // When & Then
        when(userEntityRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(postEntityRepository.findAllByUser(user,pageable)).thenReturn(Page.empty());
        Assertions.assertDoesNotThrow(() -> postService.my("", pageable));
    }


}
