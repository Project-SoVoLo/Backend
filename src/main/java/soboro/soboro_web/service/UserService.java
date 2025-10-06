package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.dto.UserUpdateRequest;
import soboro.soboro_web.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public Mono<User> register(User user) {
        return userRepository.existsByUserEmail(user.getUserEmail())
            .flatMap(duplicated -> {
                 if(duplicated){
                     return Mono.error(new RuntimeException("이미 가입된 유저입니다."));
                 }
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                return userRepository.save(user);
            });
    }

    // 이메일로 사용자 찾기
    public Mono<User> findByUserEmail(String email) {
        return userRepository.findByUserEmail(email);
    }

    // 개인정보 수정
    public Mono<User> updateUserInfo(String emailFromToken, UserUpdateRequest request){
        return userRepository.findByUserEmail(emailFromToken)
                .flatMap(user -> {
                    // 이메일 변경 시 중복체크
                   if(request.getNewEmail() != null && !request.getNewEmail().equals(emailFromToken)){
                       return userRepository.findByUserEmail(request.getNewEmail())
                               .flatMap(existing -> Mono.<User>error(new RuntimeException("이미 존재하는 이메일입니다.")))
                               .switchIfEmpty(applyUpdates(user, request));
                   } else {
                       return applyUpdates(user, request);
                   }
                });
    }

    // 실제 개인정보 필드 업데이트
    private Mono<User> applyUpdates(User user, UserUpdateRequest request){
        if(request.getNewEmail() != null) user.setUserEmail(request.getNewEmail());
        if(request.getUserName() != null) user.setUserName(request.getUserName());
        if(request.getNickname() != null) user.setNickname(request.getNickname());
        if(request.getUserPhone() != null) user.setUserPhone(request.getUserPhone());
        if(request.getNewPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        return userRepository.save(user);
    }
}
