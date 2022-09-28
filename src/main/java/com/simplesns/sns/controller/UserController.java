package com.simplesns.sns.controller;

import com.simplesns.sns.controller.request.UserJoinRequest;
import com.simplesns.sns.controller.request.UserLoginRequest;
import com.simplesns.sns.controller.response.AlarmResponse;
import com.simplesns.sns.controller.response.Response;
import com.simplesns.sns.controller.response.UserJoinResponse;
import com.simplesns.sns.controller.response.UserLoginResponse;
import com.simplesns.sns.model.User;
import com.simplesns.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody UserJoinRequest request) {
        User user = userService.join(request.getName(), request.getPassword(), request.getEmail(), request.getNickname());
        return Response.success(UserJoinResponse.formUser(user));
    }

    @PostMapping("/login")
    public Response<UserLoginResponse> login(@RequestBody UserLoginRequest request){
        String token = userService.login(request.getName(), request.getPassword());
        return Response.success(new UserLoginResponse(token));
    }

    @GetMapping("/alarm")
    public Response<Page<AlarmResponse>> alarm(Pageable pageable, Authentication authentication){
        return Response.success(userService.alarmList(authentication.getName(), pageable).map(AlarmResponse::fromAlarm));
    }
}
