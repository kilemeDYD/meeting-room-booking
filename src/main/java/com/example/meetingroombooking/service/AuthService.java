package com.example.meetingroombooking.service;

import com.example.meetingroombooking.domain.entity.User;
import com.example.meetingroombooking.repo.UserRepository;
import com.example.meetingroombooking.web.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User login(String username, String passwordPlain) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BusinessException("账号已禁用");
        }

        // 开发期：data.sql 里是 {noop}xxxx
        String stored = user.getPasswordHash();
        if (stored != null && stored.startsWith("{noop}")) {
            String raw = stored.substring("{noop}".length());
            if (!raw.equals(passwordPlain)) {
                throw new BusinessException("用户名或密码错误");
            }
            return user;
        }

        // 未来切 BCrypt：在这里扩展
        throw new BusinessException("暂不支持该密码格式，请使用开发期 {noop} 账号");
    }
}
