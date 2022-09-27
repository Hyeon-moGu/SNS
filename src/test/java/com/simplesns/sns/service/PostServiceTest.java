package com.simplesns.sns.service;

import com.simplesns.sns.exception.ErrorCode;
import com.simplesns.sns.exception.SnsApplicationException;
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

    @DisplayName("포스트작성 정상 작동")
    @Test
    void givenNothing_whenCreatePost_thenSuccessPost() throws Exception {
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
    void givenNothing_whenCreatePostNoneUser_thenReturnError() throws Exception {
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

}
