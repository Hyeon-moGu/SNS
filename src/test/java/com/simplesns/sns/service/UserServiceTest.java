package com.simplesns.sns.service;

import com.simplesns.sns.exception.ErrorCode;
import com.simplesns.sns.exception.SnsApplicationException;
import com.simplesns.sns.fixture.UserEntityFixture;
import com.simplesns.sns.model.entity.UserEntity;
import com.simplesns.sns.repository.UserEntityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserEntityRepository userEntityRepository;

    @MockBean
    private BCryptPasswordEncoder encoder;


    @DisplayName("회원가입 정상 작동")
    @Test
    void givenNothing_whenRequestSignUp_thenSuccessSignUp() {
        //Given
        String username = "username";
        String password = "password";
        String email = "email";
        String nickname = "nickname";

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(encoder.encode(password)).thenReturn("encrypt_password");
        when(userEntityRepository.save(any())).thenReturn(UserEntityFixture.get(username, password));

        Assertions.assertDoesNotThrow(() -> userService.join(username, password, email, nickname));
    }

    @DisplayName("Username 중복으로 회원가입 실패")
    @Test
    void givenNothing_whenRequestSignUpOverlapUsername_thenFailureSignUp() {
        // Given
        String username = "username";
        String password = "password";
        String email = "email";
        String nickname = "nickname";

        UserEntity fixture = UserEntityFixture.get(username, password);

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(fixture));
        when(encoder.encode(password)).thenReturn("encrypt_password");
        when(userEntityRepository.save(any())).thenReturn(Optional.of(fixture));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> userService.join(username, password, email, nickname));
        Assertions.assertEquals(ErrorCode.DUPLICATED_USER_NAME, e.getErrorCode());
    }

    @DisplayName("로그인 정상 작동")
    @Test
    void givenNothing_whenRequestLogin_thenSuccessLogin() {
        // Given
        String username = "username";
        String password = "password";

        UserEntity fixture = UserEntityFixture.get(username, password);

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(fixture));
        when(encoder.matches(password, fixture.getPassword())).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> userService.login(username, password));
    }

    @DisplayName("로그인시 없는 username으로 시도하는 경우 에러반환")
    @Test
    void givenNothing_whenRequestLoginNoneUsername_thenReturnError() {
        // Given
        String username = "username";
        String password = "password";

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.empty());

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> userService.login(username, password));
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }

    @DisplayName("로그인시 틀린 password로 시도하는 경우 에러반환")
    @Test
    void givenNothing_whenRequestLoginWrongPassword_thenReturnError() {
        // Given
        String username = "username";
        String password = "password";
        String wrongPassword = "wrongPassword";

        UserEntity fixture = UserEntityFixture.get(username, password);

        // When & Then
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(fixture));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> userService.login(username, wrongPassword));
        Assertions.assertEquals(ErrorCode.INVALID_PASSWORD, e.getErrorCode());
    }

}
