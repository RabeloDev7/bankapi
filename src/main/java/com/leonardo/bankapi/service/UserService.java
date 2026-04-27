package com.leonardo.bankapi.service;

import com.leonardo.bankapi.dto.UserRequest;
import com.leonardo.bankapi.dto.UserResponse;
import com.leonardo.bankapi.entity.User;
import com.leonardo.bankapi.exception.ResourceNotFoundException;
import com.leonardo.bankapi.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email já cadastrado: " + request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        return toResponse(userRepository.save(user));
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = findOrThrow(id);

        boolean emailTakenByOther = userRepository.findByEmail(request.getEmail())
                .filter(found -> !found.getId().equals(id))
                .isPresent();
        if (emailTakenByOther)
            throw new IllegalArgumentException("Email já cadastrado: " + request.getEmail());

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id))
            throw new ResourceNotFoundException("Usuário não encontrado: " + id);
        userRepository.deleteById(id);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
