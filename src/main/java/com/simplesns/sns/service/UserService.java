package com.simplesns.sns.service;

import com.simplesns.sns.exception.ErrorCode;
import com.simplesns.sns.exception.SnsApplicationException;
import com.simplesns.sns.model.Alarm;
import com.simplesns.sns.model.User;
import com.simplesns.sns.model.entity.UserEntity;
import com.simplesns.sns.repository.AlarmEntityRepository;
import com.simplesns.sns.repository.UserEntityRepository;
import com.simplesns.sns.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEntityRepository userEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    public User loadUserByUsername(String username){
        return userEntityRepository.findByUsername(username).map(User::fromEntity).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", username)));
    }

    @Transactional
    public User join(String username, String password, String email, String nickname) {
        // 회원가입하려는 username으로 회원가입된 user가 있는지 확인 후 user가 있다면 에러를 던짐
        userEntityRepository.findByUsername(username).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME, String.format("%s is duplicated", username));
        });
        
        // 회원가입 진행 = user등록함
        UserEntity userEntity = userEntityRepository.save(UserEntity.of(username, encoder.encode(password), email, nickname));
        return User.fromEntity(userEntity);
    }

    public String login(String username, String password) {
        // 회원가입 여부 확인
        UserEntity userEntity = userEntityRepository.findByUsername(username).orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", username)));

        // 비밀번호 체크
        if(!encoder.matches(password, userEntity.getPassword())){
            throw new SnsApplicationException(ErrorCode.INVALID_PASSWORD);
        }
        
        // 토큰 생성
        String token = JwtTokenUtils.generateToken(username, secretKey, expiredTimeMs);
        return token;
    }

    public Page<Alarm> alarmList(String username, Pageable pageable) {
        UserEntity userEntity = userEntityRepository.findByUsername(username).orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", username)));
        return alarmEntityRepository.findAllByUser(userEntity, pageable).map(Alarm::fromEntity);
    }

}
