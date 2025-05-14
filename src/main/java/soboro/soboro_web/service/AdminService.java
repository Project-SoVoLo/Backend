package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Admin;
import soboro.soboro_web.repository.AdminRepository;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<Admin> register(Admin admin) {
        return adminRepository.existsByUserEmail(admin.getUserEmail())
                .flatMap(duplicated -> {
                    if(duplicated) {
                        return Mono.error(new RuntimeException("이미 가입된 관리자 이메일 입니다."));
                    }
                    admin.setPassword(passwordEncoder.encode(admin.getPassword()));
                    return adminRepository.save(admin);
                });
    }

    public Mono<Admin> findByUserEmail(String email) {
        return adminRepository.findByUserEmail(email);
    }
}
