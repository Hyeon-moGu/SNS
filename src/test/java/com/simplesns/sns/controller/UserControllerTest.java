package com.simplesns.sns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplesns.sns.controller.request.UserJoinRequest;
import com.simplesns.sns.controller.request.UserLoginRequest;
import com.simplesns.sns.exception.ErrorCode;
import com.simplesns.sns.exception.SnsApplicationException;
import com.simplesns.sns.model.User;
import com.simplesns.sns.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("View 컨트롤러 - 회원가입, 로그인")
@SpringBootTest
@AutoConfigureMockMvc

public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @DisplayName("회원가입 - 성공")
    @Test
    public void givenSignup_whenTrySignup_thenSuccessSignup() throws Exception{
        // Given
            String username = "username";
            String password = "password";
            String email = "email";
            String nickname = "nickname";

        // When & Then
        when(userService.join(username, password, email, nickname)).thenReturn(mock(User.class));

            mockMvc.perform(post("/api/v1/users/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserJoinRequest(username, password, email, nickname)))
                    ).andDo(print())
                    .andExpect(status().isOk());
        }

    @DisplayName("회원가입 - Username 중복으로 에러반환")
    @Test
    public void givenSignup_whenTrySignup_thenReturnError() throws Exception{
        // Given
            String username = "username";
            String password = "password";
            String email = "email";
            String nickname = "nickname";

        // When & Then
        when(userService.join(username, password, email, nickname)).thenThrow(new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME));

            mockMvc.perform(post("/api/v1/users/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(new UserJoinRequest(username, password, email, nickname)))
                    ).andDo(print())
                    .andExpect(status().is(ErrorCode.DUPLICATED_USER_NAME.getStatus().value()));
    }

    @DisplayName("로그인 - 성공")
    @Test
    public void givenUsernameAndPassword_whenTryLogin_thenSuccess() throws Exception{
        // Given
        String username = "username";
        String password = "password";

        // When & Then
        when(userService.login(username, password)).thenReturn("test_token");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password)))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("로그인 - 실패 (회원가입이 되지 않은 username 이므로 에러 반환)")
    @Test
    public void givenNoneUsernameAndPassword_whenTryLogin_thenError() throws Exception {
        // Given
        String username = "username";
        String password = "password";

        // When & Then
        when(userService.login(username, password)).thenThrow(new SnsApplicationException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password)))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @DisplayName("로그인 - 실패 (틀린 password 입력하여 에러 반환)")
    @Test
    public void givenUsernameAndWrongPassword_whenTryLogin_thenError() throws Exception {
        // Given
        String username = "username";
        String password = "password";

        // When & Then
        when(userService.login(username, password)).thenThrow(new SnsApplicationException(ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

}
