package com.simplesns.sns.controller.response;

import com.simplesns.sns.model.User;
import com.simplesns.sns.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinResponse {

    private Integer id;
    private String username;
    private UserRole role;

    public static  UserJoinResponse formUser(User user){
        return new UserJoinResponse(
                user.getId(),
                user.getUsername(),
                user.getUserRole()
        );
    }

}
